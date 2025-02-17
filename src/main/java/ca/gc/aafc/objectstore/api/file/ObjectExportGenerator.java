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

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import ca.gc.aafc.objectstore.api.MainConfiguration;
import ca.gc.aafc.objectstore.api.entities.AbstractObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.storage.FileStorage;
import ca.gc.aafc.objectstore.api.util.ObjectFilenameUtils;

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
  public CompletableFuture<UUID> export(UUID exportUUID, List<AbstractObjectStoreMetadata> objectsToExport,
                                        Map<String, List<UUID>> exportLayout, Path zipFile) {

    Map<UUID, String> layoutByFileIdentifier = invertExportLayout(exportLayout);
    Map<String, AtomicInteger> filenamesIncluded = new HashMap<>();

    try (ArchiveOutputStream<ZipArchiveEntry> o = new ZipArchiveOutputStream(zipFile)) {
      for (AbstractObjectStoreMetadata currObj: objectsToExport) {

        Optional<FileObjectInfo> fileInfo =
          fileStorage.getFileInfo(currObj.getBucket(), currObj.getFilename(), currObj instanceof Derivative);

        // Set zipEntry with information from fileStorage
        ZipArchiveEntry entry =
          new ZipArchiveEntry(generateExportItemFilename(currObj, layoutByFileIdentifier, filenamesIncluded));
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
    } catch (IOException | IllegalStateException e) {
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
                                                   Map<UUID, String> layoutByFileIdentifier,
                                                   Map<String, AtomicInteger> usedFilenames) {
    String filename;

    if (obj instanceof ObjectStoreMetadata metadata) {
      filename = ObjectFilenameUtils.generateMainObjectFilename(metadata);
    } else if (obj instanceof Derivative derivative) {
      filename = ObjectFilenameUtils.generateDerivativeFilename(derivative);
    } else {
      filename = obj.getFilename();
    }

    // Do we have an export layout to consider ?
    if (layoutByFileIdentifier.containsKey(obj.getFileIdentifier())) {
      String folderName = ObjectFilenameUtils.standardizeFolderName(layoutByFileIdentifier.get(obj.getFileIdentifier()));
      filename = folderName + filename;
    }

    // if the filename is already used make sure to add a (1) at the end of the name
    if (usedFilenames.containsKey(filename)) {
      int duplicatedNumber = usedFilenames.get(filename).incrementAndGet();
      String newFilename = ObjectFilenameUtils.insertBeforeFileExtension(filename, "(" + duplicatedNumber + ")");

      // it is extremely unlikely but, we record the new generated name just in case later we have a file that
      // used that name as original filename
      usedFilenames.put(newFilename, new AtomicInteger(0));

      return newFilename;
    } else {
      usedFilenames.put(filename, new AtomicInteger(0));
    }
    return filename;
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
}
