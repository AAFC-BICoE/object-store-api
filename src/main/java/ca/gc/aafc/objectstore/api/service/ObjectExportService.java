package ca.gc.aafc.objectstore.api.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ca.gc.aafc.dina.messaging.message.ObjectExportNotification;
import ca.gc.aafc.dina.messaging.producer.DinaMessageProducer;
import ca.gc.aafc.dina.util.UUIDHelper;
import ca.gc.aafc.objectstore.api.config.ObjectExportConfiguration;
import ca.gc.aafc.objectstore.api.config.ObjectExportOption;
import ca.gc.aafc.objectstore.api.dto.ObjectExportDto;
import ca.gc.aafc.objectstore.api.entities.AbstractObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.file.ExternalResourceFileResolver;
import ca.gc.aafc.objectstore.api.file.FileObjectInfo;
import ca.gc.aafc.objectstore.api.file.ObjectExportGenerator;
import ca.gc.aafc.objectstore.api.file.ObjectExportItem;
import ca.gc.aafc.objectstore.api.file.TemporaryObjectAccessController;
import ca.gc.aafc.objectstore.api.security.FileControllerAuthorizationService;
import ca.gc.aafc.objectstore.api.storage.FileStorage;
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
  private final TemporaryObjectAccessController toaCtrl;

  private final DinaMessageProducer messageProducer;

  public ObjectExportService(ObjectExportConfiguration objectExportConfiguration,
                             ObjectExportGenerator objectExportGenerator,
                             Optional<Consumer<Future<ExportResult>>> asyncConsumer,
                             FileStorage fileStorage,
                             ExternalResourceFileResolver externalResourceFileResolver,
                             FileControllerAuthorizationService authorizationService,
                             ObjectStoreMetaDataService objectMetadataService,
                             DerivativeService derivativeService,
                             TemporaryObjectAccessController toaCtrl,
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
   * From a list of file identifiers and/or externally hosted resources, package all the files into
   * a single zip file.
   * <p>
   * Authorization will be checked on every file, if unauthorized is triggered the package will not
   * be created.
   * <p>
   * {@code fileIdentifiers} hold stored file/derivative identifiers. Externally hosted resources
   * are referenced by their metadata uuid through {@code externalResources} and are resolved by the
   * server.
   *
   * @return the uuid generated for the export
   */
  public UUID export(ExportArgs exportArgs) {

    if (StringUtils.isBlank(exportArgs.username())) {
      throw new IllegalArgumentException("username must be provided");
    }
    if (exportArgs.fileIdentifiers() == null && exportArgs.externalResources() == null) {
      throw new IllegalArgumentException("fileIdentifiers or externalResources must be provided");
    }

    ObjectExportOption exportOption = exportArgs.objectExportOption() == null
      ? ObjectExportOption.builder().build() : exportArgs.objectExportOption();

    UUID exportUUID = UUIDHelper.generateUUIDv7();

    String filename = exportUUID + EXPORT_EXT;
    Path zipFile = toaCtrl.generatePath(filename);

    List<ObjectExportItem> itemsToExport = new ArrayList<>();
    long totalSizeInBytes = 0;

    // Stored files and derivatives are referenced by their fileIdentifier
    Map<UUID, String> layoutByFileIdentifier = invertExportLayout(exportOption.exportLayout());
    Map<UUID, String> filenameAliases = exportOption.aliases() == null ? Map.of() : exportOption.aliases();

    if (exportArgs.fileIdentifiers() != null) {
      for (UUID fileIdentifier : exportArgs.fileIdentifiers()) {
        if (fileIdentifier == null) {
          throw new IllegalArgumentException("fileIdentifiers must not contain null");
        }

        AbstractObjectStoreMetadata obj = findStoredFile(fileIdentifier);
        // make sure the user is authorized before adding it to the list
        authorizationService.authorizeDownload(obj);

        totalSizeInBytes += storedFileSize(obj, fileIdentifier);
        assertWithinMaxSize(totalSizeInBytes);

        itemsToExport.add(new ObjectExportItem(obj, filenameAliases.get(fileIdentifier),
          layoutByFileIdentifier.get(fileIdentifier), null));
      }
    }

    // Externally hosted resources are referenced by their metadata uuid
    if (exportArgs.externalResources() != null) {
      Set<UUID> processedMetadataUuids = new HashSet<>();
      for (ObjectExportDto.ExternalResource externalResource : exportArgs.externalResources()) {
        UUID metadataUuid = externalResource.getMetadataUuid();
        if (metadataUuid == null) {
          throw new IllegalArgumentException("externalResources must not contain a null metadataUuid");
        }
        if (!processedMetadataUuids.add(metadataUuid)) {
          throw new IllegalArgumentException("External resource " + metadataUuid + " is listed more than once");
        }

        ObjectStoreMetadata metadata = objectMetadataService.findByUuid(metadataUuid)
          .filter(ObjectStoreMetadata::isExternal)
          .orElseThrow(() -> new IllegalArgumentException(
            "Can't find external resource with metadata uuid " + metadataUuid));

        // authorize the metadata before checking whether its backing resource is available
        authorizationService.authorizeDownload(metadata);

        Path externalPath = resolveExternalResource(metadata);
        try {
          totalSizeInBytes += Files.size(externalPath);
        } catch (IOException e) {
          throw externalResourceNotFound(metadataUuid);
        }
        assertWithinMaxSize(totalSizeInBytes);

        itemsToExport.add(new ObjectExportItem(metadata, externalResource.getFilenameAlias(),
          externalResource.getFolder(), externalPath));
      }
    }

    // then complete the export
    CompletableFuture<ExportResult> completableFuture =
      objectExportGenerator.export(exportUUID, itemsToExport, zipFile, exportOption)
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
   * Find the stored file (metadata with a fileIdentifier) or derivative identified by the given
   * fileIdentifier.
   *
   * @param fileIdentifier the file identifier
   * @return the matching metadata or derivative
   */
  private AbstractObjectStoreMetadata findStoredFile(UUID fileIdentifier) {
    Optional<Derivative> derivative = derivativeService.findByFileId(fileIdentifier);
    if (derivative.isPresent()) {
      return derivative.get();
    }
    return objectMetadataService.findByFileId(fileIdentifier)
      .orElseThrow(() -> new IllegalArgumentException("Can't find provided file identifier"));
  }

  /**
   * @return the size in bytes of the stored file/derivative.
   */
  private long storedFileSize(AbstractObjectStoreMetadata obj, UUID fileIdentifier) {
    try {
      Optional<FileObjectInfo> fileInfo =
        fileStorage.getFileInfo(obj.getBucket(), obj.getInternalFilename(), obj instanceof Derivative);
      if (fileInfo.isEmpty()) {
        throw fileNotFoundException(fileIdentifier);
      }
      return fileInfo.get().getLength();
    } catch (IOException e) {
      throw fileNotFoundException(fileIdentifier);
    }
  }

  /**
   * Resolve the backing file of an externally hosted metadata.
   * <p>
   * The metadata must have been authorized before calling this method.
   *
   * @param metadata the externally hosted metadata
   * @return the resolved path
   */
  private Path resolveExternalResource(ObjectStoreMetadata metadata) {
    String externalURL = metadata.getResourceExternalURL();
    if (!externalResourceFileResolver.isFileURL(externalURL)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
        "External resource " + metadata.getUuid() + " is not locally exportable");
    }
    Path path = externalResourceFileResolver.resolve(externalURL)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
        "External resource " + metadata.getUuid() + " is outside the configured base path"));
    if (!Files.isRegularFile(path)) {
      throw externalResourceNotFound(metadata.getUuid());
    }
    return path;
  }

  private void assertWithinMaxSize(long totalSizeInBytes) {
    // Note: this is not considering potential functions like image resize
    if (totalSizeInBytes > maxObjectExportSizeInBytes) {
      throw new IllegalStateException("Maximum export size exceeded. Max: " + maxObjectExportSizeInBytes + " bytes");
    }
  }

  private static IllegalStateException fileNotFoundException(UUID fileIdentifier) {
    return new IllegalStateException("File " + fileIdentifier + " not found");
  }

  private static ResponseStatusException externalResourceNotFound(UUID identifier) {
    return new ResponseStatusException(HttpStatus.NOT_FOUND,
      "External resource " + identifier + " is unavailable or not mounted");
  }

  private static Map<UUID, String> invertExportLayout(Map<String, List<UUID>> exportLayout) {

    if (MapUtils.isEmpty(exportLayout)) {
      return Map.of();
    }

    Map<UUID, String> layoutByFileIdentifier = new HashMap<>();
    for (var entry : exportLayout.entrySet()) {
      for (var uuid : entry.getValue()) {
        if (layoutByFileIdentifier.containsKey(uuid)) {
          throw new IllegalArgumentException("fileIdentifiers should be unique in exportLayout");
        }
        layoutByFileIdentifier.put(uuid, entry.getKey());
      }
    }
    return layoutByFileIdentifier;
  }

  /**
   * @param username           the username of the user requesting the export
   * @param fileIdentifiers    list of stored file/derivative identifiers to export
   * @param externalResources  list of externally hosted resources (by metadata uuid) to export
   * @param name               optional name of the export (optional)
   * @param objectExportOption export options (optional)
   */
  @Builder
  public record ExportArgs(String username, List<UUID> fileIdentifiers,
                           List<ObjectExportDto.ExternalResource> externalResources,
                           String name, ObjectExportOption objectExportOption) {
  }

  public record ExportResult(UUID uuid, String toaKey) {
  }

}
