package ca.gc.aafc.objectstore.api.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.SimpleFileVisitor;
import lombok.extern.log4j.Log4j2;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;

/**
 * A file visitor that detects the presence of xl.meta files in a file system directory tree.
 * <p>
 * The object-store-api using the FS storage implementation will not be able to read files uploaded
 * to a MinIO bucket that has erasure coding enabled.
 * MinIO creates xl.meta files in directories that are erasure coded, which is not compatible
 * with the Object Store API's FS file storage implementation.
 * <p>
 * The visitor traverses the FS storage file tree  and terminates immediately upon finding the first
 * xl.meta file, avoiding unnecessary traversal of the entire directory tree.
 * <p>
 * Entries that can't be read (e.g. a root-owned lost+found folder on a volume) are skipped instead of
 * aborting the traversal, so the rest of the tree is still checked. The scan is then reported as
 * incomplete.
 *
 * @see java.nio.file.SimpleFileVisitor
 */
@Log4j2
public class XlMetaFileDetector extends SimpleFileVisitor<Path> {

  private boolean foundXlMeta = false;
  private boolean scanComplete = true;

  public boolean isFoundXlMeta() {
    return foundXlMeta;
  }

  /**
   * @return false if at least one file or directory could not be read during the traversal
   */
  public boolean isScanComplete() {
    return scanComplete;
  }

  @Override
  public java.nio.file.FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
    if (attrs.isRegularFile()) {
      Path fileName = file.getFileName();
      if (fileName != null && "xl.meta".equals(fileName.toString())) {
        log.info(
          "Found xl.meta file in FS storage directory. Please ensure that the previous data in this directory was not erasure coded.");
        foundXlMeta = true;
        return TERMINATE; // Stop traversal once a .xlmeta file is found
      }
    }
    return CONTINUE;
  }

  @Override
  public java.nio.file.FileVisitResult visitFileFailed(Path file, IOException exc) {
    log.warn("Can't read {} while looking for xl.meta files: {}", file, exc.toString());
    scanComplete = false;
    return CONTINUE;
  }

  @Override
  public java.nio.file.FileVisitResult postVisitDirectory(Path dir, IOException exc) {
    // the listing of the directory failed partway
    if (exc != null) {
      return visitFileFailed(dir, exc);
    }
    return CONTINUE;
  }
}
