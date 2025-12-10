package ca.gc.aafc.objectstore.api.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * {@link FileManagement} implementation using Java {@link java.nio.file.Files} to manage files
 * using the platform File System (FS).
 */
@ConditionalOnProperty(prefix = "dina.fileStorage", name = "implementation", havingValue = "FS")
@Service
@Log4j2
public class FSFileManagement implements FileManagement {

  private final Path rootPath;

  public FSFileManagement(@Value("${dina.fileStorage.root}") String rootPath) {
    this.rootPath = Paths.get(rootPath);
  }

  @Override
  public void ensureBucketExists(String bucketName) throws IOException {
    Path bucketPath = rootPath.resolve(bucketName);

    try {
      Files.createDirectories(bucketPath);
      log.debug("Bucket ensured: {}", bucketName);
    } catch (IOException e) {
      log.error("Failed to ensure bucket: {}", bucketName, e);
      throw e;
    }
  }
}
