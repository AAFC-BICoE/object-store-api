package ca.gc.aafc.objectstore.api.service;

import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import ca.gc.aafc.objectstore.api.entities.AbstractObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.file.FileObjectInfo;
import ca.gc.aafc.objectstore.api.security.FileControllerAuthorizationService;
import ca.gc.aafc.objectstore.api.storage.FileStorage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ObjectExportService {

  private static final String EXPORT_EXT = ".zip";

  private final FileControllerAuthorizationService authorizationService;
  private final FileStorage fileStorage;
  private final ObjectStoreMetaDataService objectMetadataService;
  private final DerivativeService derivativeService;

  public ObjectExportService(FileControllerAuthorizationService authorizationService,
                             FileStorage fileStorage,
                             ObjectStoreMetaDataService objectMetadataService,
                             DerivativeService derivativeService){
    this.authorizationService = authorizationService;
    this.fileStorage = fileStorage;
    this.objectMetadataService = objectMetadataService;
    this.derivativeService = derivativeService;
  }

  /**
   * From a list of identifiers, package all the files into a single zip file.
   * Authorization will be checked on every file, if unauthorized is triggered the package will not be created.
   * @param fileIdentifiers
   * @throws IOException
   */
  public UUID export(List<UUID> fileIdentifiers) throws IOException {
    UUID exportUUID = UUID.randomUUID();

    Path zipFile = Files.createTempFile(exportUUID.toString(), EXPORT_EXT);

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
    return exportUUID;
  }

}
