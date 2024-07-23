package ca.gc.aafc.objectstore.api.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.messaging.message.ObjectExportNotification;
import ca.gc.aafc.dina.messaging.producer.DinaMessageProducer;
import ca.gc.aafc.dina.util.UUIDHelper;
import ca.gc.aafc.objectstore.api.entities.AbstractObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.file.ObjectExportGenerator;
import ca.gc.aafc.objectstore.api.file.TemporaryObjectAccessController;
import ca.gc.aafc.objectstore.api.security.FileControllerAuthorizationService;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class ObjectExportService {

  private static final String EXPORT_EXT = ".zip";

  private final ObjectExportGenerator objectExportGenerator;
  private final Consumer<Future<ExportResult>> asyncConsumer;

  private final FileControllerAuthorizationService authorizationService;
  private final ObjectStoreMetaDataService objectMetadataService;
  private final DerivativeService derivativeService;
  private final TemporaryObjectAccessController toaCtrl;

  private final DinaMessageProducer messageProducer;

  public ObjectExportService(ObjectExportGenerator objectExportGenerator,
                             Optional<Consumer<Future<ExportResult>>> asyncConsumer,
                             FileControllerAuthorizationService authorizationService,
                             ObjectStoreMetaDataService objectMetadataService,
                             DerivativeService derivativeService,
                             TemporaryObjectAccessController toaCtrl,
                             DinaMessageProducer messageProducer) {

    this.objectExportGenerator = objectExportGenerator;
    this.asyncConsumer = asyncConsumer.orElse(null);

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
   * @param username the username of the user requesting the export
   * @param fileIdentifiers list of files identifier to export
   * @param name optional name of the export
   * @return the uuid generated for the export
   */
  public UUID export(String username, List<UUID> fileIdentifiers, String name) {
    UUID exportUUID = UUIDHelper.generateUUIDv7();

    String filename = exportUUID + EXPORT_EXT;
    Path zipFile = toaCtrl.generatePath(filename);
    List<AbstractObjectStoreMetadata> toExport = new ArrayList<>(fileIdentifiers.size());

    for (UUID fileIdentifier : fileIdentifiers) {
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
      toExport.add(obj);
    }

    // then complete the export
    CompletableFuture<ExportResult> completableFuture =
      objectExportGenerator.export(exportUUID, toExport, zipFile).thenApply(uuid -> {
        String toaKey = toaCtrl.registerObject(filename);
        log.info("Generated toaKey {}", () -> toaKey);

        ObjectExportNotification.ObjectExportNotificationBuilder oenBuilder =
          ObjectExportNotification.builder()
            .uuid(exportUUID)
            .username(username)
            .name(filename)
            .toa(toaKey);

        if (StringUtils.isNotBlank(name)) {
          oenBuilder.name(name);
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

  public record ExportResult(UUID uuid, String toaKey) {
  }

}
