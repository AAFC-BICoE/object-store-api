package ca.gc.aafc.objectstore.api.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;

import ca.gc.aafc.objectstore.api.file.FolderStructureStrategy;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FSFileStorageIT {

  public static final String BUCKET = "bucket";

  @TempDir
  static Path tempDir;

  @Test
  public void testFSFileStorage() throws IOException {
    FolderStructureStrategy folderStructureStrategy = new FolderStructureStrategy();
    FSFileStorage fileStorage = new FSFileStorage(tempDir.toString(), folderStructureStrategy);

    byte[] bytes = "dina".getBytes();
    String fileName = "name.txt";

    fileStorage.storeFile(
      BUCKET,
      fileName,
      false,
      MediaType.TEXT_PLAIN_VALUE,
      new ByteArrayInputStream(bytes)
    );

    assertTrue(tempDir.resolve(BUCKET)
      .resolve(folderStructureStrategy.getPathFor(fileName, false))
      .toFile().exists());
  }
}
