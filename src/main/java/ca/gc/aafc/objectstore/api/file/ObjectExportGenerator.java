package ca.gc.aafc.objectstore.api.file;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import ca.gc.aafc.objectstore.api.MainConfiguration;
import ca.gc.aafc.objectstore.api.entities.AbstractObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.storage.FileStorage;

/**
 * Responsible to generate the export archive asynchronously.
 */
@Service
public class ObjectExportGenerator {

  private final FileStorage fileStorage;

  public ObjectExportGenerator(FileStorage fileStorage) {
    this.fileStorage = fileStorage;
  }

  @Async(MainConfiguration.DINA_THREAD_POOL_BEAN_NAME)
  public CompletableFuture<UUID> export(UUID exportUUID, List<AbstractObjectStoreMetadata> objectsToExport, Path zipFile) {
    Map<String, AtomicInteger> filenamesIncluded = new HashMap<>();

    try (ArchiveOutputStream<ZipArchiveEntry> o = new ZipArchiveOutputStream(zipFile)) {
      for (AbstractObjectStoreMetadata currObj: objectsToExport) {

        Optional<FileObjectInfo> fileInfo =
          fileStorage.getFileInfo(currObj.getBucket(), currObj.getFilename(), currObj instanceof Derivative);

        // Set zipEntry with information from fileStorage
        ZipArchiveEntry entry =
          new ZipArchiveEntry(generateExportItemFilename(currObj, filenamesIncluded));
        entry.setSize(
          fileInfo.orElseThrow(() -> new IllegalStateException("No FileInfo found")).getLength());
        o.putArchiveEntry(entry);

        // Get and copy the stream into the zip
        Optional<InputStream> optIs =
          fileStorage.retrieveFile(currObj.getBucket(), currObj.getFilename(), currObj instanceof Derivative);
        try (InputStream is = optIs.orElseThrow(
          () -> new IllegalStateException("No InputStream available"))) {
          IOUtils.copy(is, o);
        }
        o.closeArchiveEntry();
      }
      o.finish();
    } catch (IOException e) {
      return CompletableFuture.failedFuture(e);
    }

    return CompletableFuture.completedFuture(exportUUID);
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

}