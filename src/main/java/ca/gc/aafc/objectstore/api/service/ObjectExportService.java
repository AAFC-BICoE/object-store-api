package ca.gc.aafc.objectstore.api.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.messaging.message.ObjectExportNotification;
import ca.gc.aafc.dina.messaging.producer.DinaMessageProducer;
import ca.gc.aafc.dina.util.UUIDHelper;
import ca.gc.aafc.objectstore.api.config.ObjectExportConfiguration;
import ca.gc.aafc.objectstore.api.config.ObjectExportOption;
import ca.gc.aafc.objectstore.api.entities.AbstractObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.file.ExternalResourceFileResolver;
import ca.gc.aafc.objectstore.api.file.FileObjectInfo;
import ca.gc.aafc.objectstore.api.file.ObjectExportGenerator;
import ca.gc.aafc.objectstore.api.file.ObjectExportItem;
import ca.gc.aafc.objectstore.api.file.ObjectStoreTOAController;
import ca.gc.aafc.objectstore.api.security.FileControllerAuthorizationService;
import ca.gc.aafc.objectstore.api.storage.FileStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class ObjectExportService {

  private static final String EXPORT_EXT = ".zip";

  private final long maxObjectExportSizeInBytes;
  private final FileStorage fileStorage;
  private final ObjectExportGenerator objectExportGenerator;
  private final ExternalResourceFileResolver externalResourceFileResolver;
  private final Consumer<Future<ExportResult>> asyncConsumer;

  private final FileControllerAuthorizationService authorizationService;
  private final ObjectStoreMetaDataService objectMetadataService;
  private final DerivativeService derivativeService;
  private final ObjectStoreTOAController toaCtrl;

  private final DinaMessageProducer messageProducer;

  public ObjectExportService(ObjectExportConfiguration objectExportConfiguration,
                             ObjectExportGenerator objectExportGenerator,
                             Optional<Consumer<Future<ExportResult>>> asyncConsumer,
                             FileStorage fileStorage,
                             ExternalResourceFileResolver externalResourceFileResolver,
                             FileControllerAuthorizationService authorizationService,
                             ObjectStoreMetaDataService objectMetadataService,
                             DerivativeService derivativeService,
                             ObjectStoreTOAController toaCtrl,
                             DinaMessageProducer messageProducer) {

    maxObjectExportSizeInBytes = objectExportConfiguration.getMaxObjectExportSize().toBytes();
    this.objectExportGenerator = objectExportGenerator;
    this.asyncConsumer = asyncConsumer.orElse(null);

    this.fileStorage = fileStorage;
    this.externalResourceFileResolver = externalResourceFileResolver;
    this.authorizationService = authorizationService;
    this.objectMetadataService = objectMetadataService;
    this.derivativeService = derivativeService;
    this.toaCtrl = toaCtrl;
    this.messageProducer = messageProducer;
  }

  /**
   * From a list of identifiers, package all the files into a single zip file.
   * Authorization will be checked on every file, if unauthorized is triggered the package will not be created.
   * <p>
   * Stored files and derivatives are identified by their fileIdentifier. Externally hosted resources
   * have no fileIdentifier: they are identified by their metadata uuid and read from the local file
   * system (see {@link ExternalResourceFileResolver}).
   *
   * @return the uuid generated for the export
   */
  public UUID export(ExportArgs exportArgs) {

    if (StringUtils.isBlank(exportArgs.username()) || exportArgs.fileIdentifiers() == null) {
      throw new IllegalArgumentException("username and fileIdentifiers must be provided");
    }

    UUID exportUUID = UUIDHelper.generateUUIDv7();

    String filename = exportUUID + EXPORT_EXT;
    Path zipFile = toaCtrl.generatePath(filename);
    Map<UUID, String> folderByFileIdentifier =
      invertExportLayout(exportArgs.objectExportOption().exportLayout());
    Map<UUID, String> filenameAliases = exportArgs.objectExportOption().aliases() == null
      ? Map.of() : exportArgs.objectExportOption().aliases();
    List<ObjectExportItem> toExport = new ArrayList<>(exportArgs.fileIdentifiers().size());
    long totalSizeInBytes = 0;

    for (UUID fileIdentifier : exportArgs.fileIdentifiers()) {
      AbstractObjectStoreMetadata obj = findMetadataToExport(fileIdentifier);

      // make sure the user is authorized before adding it to the list
      authorizationService.authorizeDownload(obj);

      // make sure the file exists and compute total size
      ObjectExportItem exportItem = createExportItem(obj,
        filenameAliases.get(fileIdentifier), folderByFileIdentifier.get(fileIdentifier));
      totalSizeInBytes += getFileSize(exportItem)
        .orElseThrow(() -> fileNotFound(fileIdentifier));

      // validate total size
      // Note: this is not considering potential functions like image resize
      if (totalSizeInBytes > maxObjectExportSizeInBytes) {
        throw new IllegalStateException("Maximum export size exceeded. Max: " + maxObjectExportSizeInBytes + " bytes");
      }

      toExport.add(exportItem);
    }

    // then complete the export
    CompletableFuture<ExportResult> completableFuture =
      objectExportGenerator.export(exportUUID, toExport, zipFile, exportArgs.objectExportOption())
        .thenApply(uuid -> {
          String toaKey = toaCtrl.registerObject(filename);
          log.info("Generated toaKey {}", () -> toaKey);

          ObjectExportNotification.ObjectExportNotificationBuilder oenBuilder =
            ObjectExportNotification.builder()
              .uuid(exportUUID)
              .username(exportArgs.username())
              .name(filename)
              .toa(toaKey);

          if (StringUtils.isNotBlank(exportArgs.name())) {
            oenBuilder.name(exportArgs.name());
          }

          messageProducer.send(oenBuilder.build());
          return new ExportResult(exportUUID, toaKey);
        })
        // if exception
        .exceptionally(ex -> {
          log.error("Async exception:", ex);
          return null;
        });

    if (asyncConsumer != null) {
      asyncConsumer.accept(completableFuture);
    }

    return exportUUID;
  }

  /**
   * Find the metadata to export: a derivative or a stored file by fileIdentifier, otherwise an
   * externally hosted resource by its metadata uuid.
   *
   * @param identifier fileIdentifier, or metadata uuid for externally hosted resources
   * @return the metadata
   * @throws IllegalArgumentException if nothing can be found for the identifier
   */
  private AbstractObjectStoreMetadata findMetadataToExport(UUID identifier) {
    Optional<Derivative> derivative = derivativeService.findByFileId(identifier);
    if (derivative.isPresent()) {
      return derivative.get();
    }

    return objectMetadataService.findByFileId(identifier)
      .or(() -> objectMetadataService.findByUuid(identifier)
        .filter(ObjectStoreMetadata::isExternal))
      .orElseThrow(() -> new IllegalArgumentException("Can't find provided fileIdentifier"));
  }

  /**
   * Create the export item. For externally hosted resources, the resourceExternalURL is resolved
   * to a local path.
   *
   * @throws IllegalStateException if an externally hosted resource can't be resolved
   */
  private ObjectExportItem createExportItem(AbstractObjectStoreMetadata obj, String filenameAlias,
                                            String folder) {
    if (obj instanceof ObjectStoreMetadata metadata && metadata.isExternal()) {
      Path externalPath = externalResourceFileResolver.resolve(metadata.getResourceExternalURL())
        .orElseThrow(() -> fileNotFound(metadata.getUuid()));
      return new ObjectExportItem.ExternalResource(metadata, filenameAlias, folder, externalPath);
    }
    return new ObjectExportItem.StoredFile(obj, filenameAlias, folder);
  }

  /**
   * @return the size of the file in bytes, empty if the file can't be found
   */
  private Optional<Long> getFileSize(ObjectExportItem exportItem) {
    try {
      return switch (exportItem) {
        case ObjectExportItem.StoredFile stored -> fileStorage.getFileInfo(
            stored.metadata().getBucket(), stored.metadata().getInternalFilename(),
            stored.metadata() instanceof Derivative)
          .map(FileObjectInfo::getLength);
        case ObjectExportItem.ExternalResource external ->
          Files.isRegularFile(external.externalPath())
            ? Optional.of(Files.size(external.externalPath())) : Optional.empty();
      };
    } catch (IOException e) {
      return Optional.empty();
    }
  }

  private static IllegalStateException fileNotFound(UUID fileIdentifier) {
    return new IllegalStateException("File " + fileIdentifier + " not found");
  }

  private static Map<UUID, String> invertExportLayout(Map<String, List<UUID>> exportLayout) {
    if (exportLayout == null || exportLayout.isEmpty()) {
      return Map.of();
    }

    Map<UUID, String> folderByFileIdentifier = new HashMap<>();
    for (var entry : exportLayout.entrySet()) {
      for (var fileIdentifier : entry.getValue()) {
        if (folderByFileIdentifier.containsKey(fileIdentifier)) {
          throw new IllegalArgumentException("fileIdentifiers should be unique in exportLayout");
        }
        folderByFileIdentifier.put(fileIdentifier, entry.getKey());
      }
    }
    return folderByFileIdentifier;
  }

  /**
   * @param username           the username of the user requesting the export
   * @param fileIdentifiers    list of files identifier to export (metadata uuid for externally
   *                           hosted resources)
   * @param name               optional name of the export (optional)
   * @param objectExportOption export options (optional)
   */
  @Builder
  public record ExportArgs(String username, List<UUID> fileIdentifiers,
                           String name, ObjectExportOption objectExportOption) {
  }

  public record ExportResult(UUID uuid, String toaKey) {
  }

}
