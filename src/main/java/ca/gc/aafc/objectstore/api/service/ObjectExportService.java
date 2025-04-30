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
import ca.gc.aafc.objectstore.api.file.FileObjectInfo;
import ca.gc.aafc.objectstore.api.file.ObjectExportGenerator;
import ca.gc.aafc.objectstore.api.file.TemporaryObjectAccessController;
import ca.gc.aafc.objectstore.api.security.FileControllerAuthorizationService;
import ca.gc.aafc.objectstore.api.storage.FileStorage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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
                             FileControllerAuthorizationService authorizationService,
                             ObjectStoreMetaDataService objectMetadataService,
                             DerivativeService derivativeService,
                             TemporaryObjectAccessController toaCtrl,
                             DinaMessageProducer messageProducer) {

    maxObjectExportSizeInBytes = objectExportConfiguration.getMaxObjectExportSize().toBytes();
    this.objectExportGenerator = objectExportGenerator;
    this.asyncConsumer = asyncConsumer.orElse(null);

    this.fileStorage = fileStorage;
    this.authorizationService = authorizationService;
    this.objectMetadataService = objectMetadataService;
    this.derivativeService = derivativeService;
    this.toaCtrl = toaCtrl;
    this.messageProducer = messageProducer;
  }

  /**
   * From a list of identifiers, package all the files into a single zip file.
   * Authorization will be checked on every file, if unauthorized is triggered the package will not be created.
   *
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
    List<AbstractObjectStoreMetadata> toExport = new ArrayList<>(exportArgs.fileIdentifiers().size());
    long totalSizeInBytes = 0;

    for (UUID fileIdentifier : exportArgs.fileIdentifiers()) {
      AbstractObjectStoreMetadata obj;
      Optional<Derivative> derivative = derivativeService.findByFileId(fileIdentifier);

      if (derivative.isPresent()) {
        obj = derivative.get();
      } else {
        Optional<ObjectStoreMetadata> objectMetadata =
          objectMetadataService.findByFileId(fileIdentifier);
        obj = objectMetadata.orElseThrow(
          () -> new IllegalArgumentException("Can't find provided fileIdentifier"));
      }
      // make sure the user is authorized before adding it to the list
      authorizationService.authorizeDownload(obj);

      // get file info to make sure the file exists and compute total size
      try {
        Optional<FileObjectInfo> fileInfo =
          fileStorage.getFileInfo(obj.getBucket(), obj.getFilename(), obj instanceof Derivative);
        if (fileInfo.isPresent()) {
          totalSizeInBytes += fileInfo.get().getLength();
        } else {
          throwIllegalStateFileNotFound(fileIdentifier);
        }
      } catch (IOException e) {
        throwIllegalStateFileNotFound(fileIdentifier);
      }

      // validate total size
      if (totalSizeInBytes > maxObjectExportSizeInBytes) {
        throw new IllegalStateException("Maximum export size exceeded. Max: " + maxObjectExportSizeInBytes + " bytes");
      }

      toExport.add(obj);
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

  private static void throwIllegalStateFileNotFound(UUID fileIdentifier) throws IllegalStateException {
    throw new IllegalStateException("File " + fileIdentifier + " not found");
  }

  /**
   * @param username           the username of the user requesting the export
   * @param fileIdentifiers    list of files identifier to export
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
