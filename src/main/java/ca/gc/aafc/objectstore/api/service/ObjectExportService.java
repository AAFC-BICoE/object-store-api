package ca.gc.aafc.objectstore.api.service;

import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.messaging.message.ObjectExportNotification;
import ca.gc.aafc.dina.util.UUIDHelper;
import ca.gc.aafc.objectstore.api.entities.AbstractObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.file.FileObjectInfo;
import ca.gc.aafc.objectstore.api.file.TemporaryObjectAccessController;
import ca.gc.aafc.objectstore.api.messaging.ObjectExportMessageProducer;
import ca.gc.aafc.objectstore.api.security.FileControllerAuthorizationService;
import ca.gc.aafc.objectstore.api.storage.FileStorage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class ObjectExportService {

  private static final String EXPORT_EXT = ".zip";

  private final FileControllerAuthorizationService authorizationService;
  private final FileStorage fileStorage;
  private final ObjectStoreMetaDataService objectMetadataService;
  private final DerivativeService derivativeService;
  private final TemporaryObjectAccessController toaCtrl;

  private final ObjectExportMessageProducer objectExportMessageProducer;

  public ObjectExportService(FileControllerAuthorizationService authorizationService,
                             FileStorage fileStorage,
                             ObjectStoreMetaDataService objectMetadataService,
                             DerivativeService derivativeService,
                             TemporaryObjectAccessController toaCtrl,
                             ObjectExportMessageProducer objectExportMessageProducer) {
    this.authorizationService = authorizationService;
    this.fileStorage = fileStorage;
    this.objectMetadataService = objectMetadataService;
    this.derivativeService = derivativeService;
    this.toaCtrl = toaCtrl;
    this.objectExportMessageProducer = objectExportMessageProducer;
  }

  /**
   * From a list of identifiers, package all the files into a single zip file.
   * Authorization will be checked on every file, if unauthorized is triggered the package will not be created.
   * @param fileIdentifiers
   * @throws IOException
   */
  public ExportResult export(String username, List<UUID> fileIdentifiers, String name) throws IOException {
    UUID exportUUID = UUIDHelper.generateUUIDv7();

    String filename = (StringUtils.isBlank(name) ? exportUUID.toString() : name) + EXPORT_EXT;
    Path zipFile = toaCtrl.generatePath(filename);
    try (ArchiveOutputStream o = new ZipArchiveOutputStream(zipFile)) {
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

        authorizationService.authorizeDownload(obj);
        Optional<FileObjectInfo> fileInfo = fileStorage.getFileInfo(obj.getBucket(), obj.getFilename(), derivative.isPresent());

        // Set zipEntry with information from fileStorage
        ZipArchiveEntry entry = new ZipArchiveEntry(obj.getFilename());
        entry.setSize(fileInfo.orElseThrow(() -> new IllegalStateException("No FileInfo found")).getLength());
        o.putArchiveEntry(entry);

        // Get and copy the stream into the zip
        Optional<InputStream> optIs =
          fileStorage.retrieveFile(obj.getBucket(), obj.getFilename(), derivative.isPresent());
        try (InputStream is = optIs.orElseThrow(
          () -> new IllegalStateException("No InputStream available"))) {
          IOUtils.copy(is, o);
        }
        o.closeArchiveEntry();
      }
      o.finish();
    }
    String toaKey = toaCtrl.registerObject(filename);
    log.info("Generated toaKey {}", () -> toaKey);

    ObjectExportNotification oen = ObjectExportNotification.builder()
      .uuid(exportUUID)
      .username(username)
      .name(filename)
      .toa(toaKey)
      .build();

    objectExportMessageProducer.send(oen);

    return new ExportResult(exportUUID, toaKey);
  }

  public record ExportResult(UUID uuid, String toaKey) {
  }

}
