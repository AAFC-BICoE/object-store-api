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
import java.util.LinkedHashMap;
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
   * have no fileIdentifier: they are identified by their metadata uuid, provided separately in
   * externalResourceIds, and read from the local file system
   * (see {@link ExternalResourceFileResolver}).
   *
   * @return the uuid generated for the export
   */
  public UUID export(ExportArgs exportArgs) {

    List<UUID> fileIdentifiers = exportArgs.fileIdentifiers() == null
      ? List.of() : exportArgs.fileIdentifiers();
    List<UUID> externalResourceIds = exportArgs.externalResourceIds() == null
      ? List.of() : exportArgs.externalResourceIds();

    if (StringUtils.isBlank(exportArgs.username())
        || fileIdentifiers.isEmpty() && externalResourceIds.isEmpty()) {
      throw new IllegalArgumentException(
        "username and at least one of fileIdentifiers or externalResourceIds must be provided");
    }

    UUID exportUUID = UUIDHelper.generateUUIDv7();

    String filename = exportUUID + EXPORT_EXT;
    Path zipFile = toaCtrl.generatePath(filename);
    Map<UUID, String> folderByIdentifier =
      invertExportLayout(exportArgs.objectExportOption().exportLayout());
    Map<UUID, String> filenameAliases = exportArgs.objectExportOption().aliases() == null
      ? Map.of() : exportArgs.objectExportOption().aliases();

    // identifier as provided by the user (used for layout and aliases) -> metadata
    Map<UUID, AbstractObjectStoreMetadata> metadataToExport = new LinkedHashMap<>();
    for (UUID fileIdentifier : fileIdentifiers) {
      metadataToExport.put(fileIdentifier, findByFileIdentifier(fileIdentifier));
    }
    for (UUID externalResourceId : externalResourceIds) {
      metadataToExport.put(externalResourceId, findExternalResource(externalResourceId));
    }

    List<ObjectExportItem> toExport = new ArrayList<>(metadataToExport.size());
    long totalSizeInBytes = 0;

    for (Map.Entry<UUID, AbstractObjectStoreMetadata> entry : metadataToExport.entrySet()) {
      UUID identifier = entry.getKey();
      AbstractObjectStoreMetadata obj = entry.getValue();

      // make sure the user is authorized before adding it to the list
      authorizationService.authorizeDownload(obj);

      // make sure the file exists and compute total size
      ObjectExportItem exportItem = createExportItem(obj,
        filenameAliases.get(identifier), folderByIdentifier.get(identifier));
      totalSizeInBytes += getFileSize(exportItem)
        .orElseThrow(() -> throwFileNotFound(identifier));

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
   * Find the metadata of a derivative or a stored file by fileIdentifier.
   *
   * @throws IllegalArgumentException if nothing can be found for the fileIdentifier
   */
  private AbstractObjectStoreMetadata findByFileIdentifier(UUID fileIdentifier) {
    Optional<Derivative> derivative = derivativeService.findByFileId(fileIdentifier);
    if (derivative.isPresent()) {
      return derivative.get();
    }

    return objectMetadataService.findByFileId(fileIdentifier)
      .orElseThrow(() -> new IllegalArgumentException("Can't find provided fileIdentifier"));
  }

  /**
   * Find the metadata of an externally hosted resource by its metadata uuid.
   *
   * @throws IllegalArgumentException if the uuid doesn't match an externally hosted resource
   */
  private ObjectStoreMetadata findExternalResource(UUID externalResourceId) {
    return objectMetadataService.findByUuid(externalResourceId)
      .filter(ObjectStoreMetadata::isExternal)
      .orElseThrow(() -> new IllegalArgumentException("Can't find provided externalResourceId"));
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
        .orElseThrow(() -> throwFileNotFound(metadata.getUuid()));
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

  private static IllegalStateException throwFileNotFound(UUID fileIdentifier) {
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
   * @param fileIdentifiers    list of files identifier (stored files and derivatives) to export
   * @param externalResourceIds list of metadata uuid of externally hosted resources to export
   * @param name               optional name of the export (optional)
   * @param objectExportOption export options (optional)
   */
  @Builder
  public record ExportArgs(String username, List<UUID> fileIdentifiers,
                           List<UUID> externalResourceIds, String name, ObjectExportOption objectExportOption) {
  }

  public record ExportResult(UUID uuid, String toaKey) {
  }

}
