package ca.gc.aafc.objectstore.api.service;

import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.messaging.message.ObjectExportNotification;
import ca.gc.aafc.dina.messaging.producer.DinaMessageProducer;
import ca.gc.aafc.dina.util.UUIDHelper;
import ca.gc.aafc.objectstore.api.entities.AbstractObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.file.FileController;
import ca.gc.aafc.objectstore.api.file.FileObjectInfo;
import ca.gc.aafc.objectstore.api.file.TemporaryObjectAccessController;
import ca.gc.aafc.objectstore.api.security.FileControllerAuthorizationService;
import ca.gc.aafc.objectstore.api.storage.FileStorage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
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

  private final DinaMessageProducer messageProducer;

  public ObjectExportService(FileControllerAuthorizationService authorizationService,
                             FileStorage fileStorage,
                             ObjectStoreMetaDataService objectMetadataService,
                             DerivativeService derivativeService,
                             TemporaryObjectAccessController toaCtrl,
                             DinaMessageProducer messageProducer) {
    this.authorizationService = authorizationService;
    this.fileStorage = fileStorage;
    this.objectMetadataService = objectMetadataService;
    this.derivativeService = derivativeService;
    this.toaCtrl = toaCtrl;
    this.messageProducer = messageProducer;
  }

  /**
   * From a list of identifiers, package all the files into a single zip file.
   * Authorization will be checked on every file, if unauthorized is triggered the package will not be created.
   *
   * @param fileIdentifiers
   * @throws IOException
   */
  public ExportResult export(String username, List<UUID> fileIdentifiers, String name)
    throws IOException {
    UUID exportUUID = UUIDHelper.generateUUIDv7();

    String filename = exportUUID + EXPORT_EXT;
    Path zipFile = toaCtrl.generatePath(filename);
    Map<String, AtomicInteger> filenamesIncluded = new HashMap<>();
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
        Optional<FileObjectInfo> fileInfo =
          fileStorage.getFileInfo(obj.getBucket(), obj.getFilename(), derivative.isPresent());

        // Set zipEntry with information from fileStorage
        ZipArchiveEntry entry =
          new ZipArchiveEntry(generateExportItemFilename(obj, filenamesIncluded));
        entry.setSize(
          fileInfo.orElseThrow(() -> new IllegalStateException("No FileInfo found")).getLength());
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
  }

  /**
   * Get a unique (withing the export) filename.
   *
   * @param obj           the data about the file to add
   * @param usedFilenames filenames that are already used with a counter. Will be modified by this function.
   * @return
   */
  private static String generateExportItemFilename(AbstractObjectStoreMetadata obj,
                                                   Map<String, AtomicInteger> usedFilenames) {
    String filename;

    if (obj instanceof ObjectStoreMetadata metadata) {
      filename = FileController.generateDownloadFilename(metadata.getOriginalFilename(),
        metadata.getFilename(), metadata.getFileExtension());
    } else if (obj instanceof Derivative derivative) {
      filename = FileController.generateDownloadFilename(
        generateDerivativeExportItemFilename(derivative), derivative.getFilename(),
        derivative.getFileExtension());
    } else {
      filename = obj.getFilename();
    }

    // if the filename is already used make sure to add a (1) at the end of the name
    if (usedFilenames.containsKey(filename)) {
      int duplicatedNumber = usedFilenames.get(filename).incrementAndGet();
      String newFilename = insertBeforeFileExtension(filename, "(" + duplicatedNumber + ")");

      // it is extremely unlikely but, we record the new generated name just in case later we have a file that
      // used that name as original filename
      usedFilenames.put(newFilename, new AtomicInteger(0));

      return newFilename;
    } else {
      usedFilenames.put(filename, new AtomicInteger(0));
    }

    return filename;
  }

  /**
   * Generate the export item name. Since Derivatives don't have a name we are trying to use the name
   * of the derivedFrom and add the derivative type as suffix. We will use a fallback on derivative's uuid.
   * @param derivative
   * @return
   */
  private static String generateDerivativeExportItemFilename(Derivative derivative) {
    ObjectStoreMetadata derivedFrom = derivative.getAcDerivedFrom();
    if (derivedFrom != null) {
      String derivativeSuffix =
        derivative.getDerivativeType() != null ? derivative.getDerivativeType().getSuffix() :
          "derivative";
      return insertBeforeFileExtension(derivedFrom.getOriginalFilename(), "_" + derivativeSuffix);
    }
    return derivative.getUuid().toString();
  }

  /**
   * Insert a specific string just before the extensions in the filename.
   * @param filename
   * @param toInsert
   * @return
   */
  private static String insertBeforeFileExtension(String filename, String toInsert) {
    StringBuilder newFilename = new StringBuilder(filename);
    newFilename.insert(FilenameUtils.indexOfExtension(filename), toInsert);
    return newFilename.toString();
  }

  public record ExportResult(UUID uuid, String toaKey) {
  }

}
