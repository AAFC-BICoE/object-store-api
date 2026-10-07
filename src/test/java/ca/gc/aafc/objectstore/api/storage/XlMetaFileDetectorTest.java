package ca.gc.aafc.objectstore.api.storage;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class XlMetaFileDetectorTest {

  @TempDir
  Path tempDir;

  @Test
  public void walk_whenNoXlMeta_scanCompleteNothingFound() throws IOException {
    createFile(tempDir.resolve("bucket/ab/cd/abcd1234.txt"));
    createFile(tempDir.resolve("bucket/derivatives/ab/cd/abcd5678.jpg"));

    XlMetaFileDetector detector = new XlMetaFileDetector();
    Files.walkFileTree(tempDir, detector);

    assertFalse(detector.isFoundXlMeta());
    assertTrue(detector.isScanComplete());
  }

  @Test
  public void walk_whenXlMeta_found() throws IOException {
    createFile(tempDir.resolve("bucket/ab/cd/abcd1234.txt/xl.meta"));

    XlMetaFileDetector detector = new XlMetaFileDetector();
    Files.walkFileTree(tempDir, detector);

    assertTrue(detector.isFoundXlMeta());
  }

  @Test
  public void visitFileFailed_continuesAndFlagsScanIncomplete() {
    XlMetaFileDetector detector = new XlMetaFileDetector();
    Path lostAndFound = tempDir.resolve("lost+found");

    assertEquals(FileVisitResult.CONTINUE,
      detector.visitFileFailed(lostAndFound, new AccessDeniedException(lostAndFound.toString())));
    assertFalse(detector.isScanComplete());
    assertFalse(detector.isFoundXlMeta());
  }

  @Test
  public void postVisitDirectory_whenListingFailed_continuesAndFlagsScanIncomplete() {
    XlMetaFileDetector detector = new XlMetaFileDetector();

    assertEquals(FileVisitResult.CONTINUE, detector.postVisitDirectory(tempDir, null));
    assertTrue(detector.isScanComplete());

    assertEquals(FileVisitResult.CONTINUE,
      detector.postVisitDirectory(tempDir, new IOException("listing failed")));
    assertFalse(detector.isScanComplete());
  }

  private static void createFile(Path file) throws IOException {
    Files.createDirectories(file.getParent());
    Files.writeString(file, "dina");
  }
}
