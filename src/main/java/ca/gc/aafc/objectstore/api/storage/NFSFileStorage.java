package ca.gc.aafc.objectstore.api.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import ca.gc.aafc.objectstore.api.file.FileObjectInfo;
import ca.gc.aafc.objectstore.api.file.FolderStructureStrategy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;

@ConditionalOnProperty(prefix = "dina.fileStorage", name = "implementation", havingValue = "NFS")
@Service
@Log4j2
public class NFSFileStorage implements FileStorage {

  private final Path rootPath;

  private final FolderStructureStrategy folderStructureStrategy;

  /**
   * @param rootPath Base directory for storage (e.g., "/mnt/nfs")
   */
  public NFSFileStorage(@Value("${dina.fileStorage.root}") String rootPath, FolderStructureStrategy folderStructureStrategy) {
    this.rootPath = Paths.get(rootPath);
    this.folderStructureStrategy = folderStructureStrategy;
  }

  @Override
  public void storeFile(String bucket, String fileName, boolean isDerivative,
                        String contentType, InputStream iStream) throws IOException {

    Path filePath = getFilePath(bucket, fileName, isDerivative);
    try {
      // Write stream to temp file with fsync
      saveFileNFS(filePath, iStream);

      log.info("Stored file. bucket:{} : {}", bucket, fileName);

    } catch (IOException e) {
     // deleteTempFile(tempFile);
      log.error("Failed to store file: {}/{}", bucket, fileName, e);
      throw e;
    }
  }

  @Override
  public Optional<InputStream> retrieveFile(String bucket, String fileName,
                                            boolean isDerivative) throws IOException {

    Path filePath = getFilePath(bucket, fileName, isDerivative);

    if (!Files.exists(filePath)) {
      log.debug("File not found: {}/{}", bucket, fileName);
      return Optional.empty();
    }

    try {
      InputStream is = Files.newInputStream(filePath);
      log.info("Retrieved file: {}/{}", bucket, fileName);
      return Optional.of(is);
    } catch (IOException e) {
      log.error("Failed to retrieve file: {}/{}", bucket, fileName, e);
      throw e;
    }
  }

  @Override
  public void deleteFile(String bucket, String fileName, boolean isDerivative)
    throws IOException {

    Path filePath = getFilePath(bucket, fileName, isDerivative);

    try {
      Files.deleteIfExists(filePath);
      log.info("Deleted file: {}/{}", bucket, fileName);
    } catch (IOException e) {
      log.error("Failed to delete file: {}/{}", bucket, fileName, e);
      throw e;
    }
  }

  @Override
  public Optional<FileObjectInfo> getFileInfo(String bucketName, String fileName,
                                              boolean isDerivative) throws IOException {

    Path filePath = getFilePath(bucketName, fileName, isDerivative);

    if (!Files.exists(filePath)) {
      return Optional.empty();
    }

    try {
      long size = Files.size(filePath);
      String contentType = Files.probeContentType(filePath);
      long lastModified = Files.getLastModifiedTime(filePath).toMillis();

      FileObjectInfo info = FileObjectInfo.builder()
        .length(size)
        .contentType(contentType)
        .build();

      log.debug("Got file info: {}/{}", bucketName, fileName);
      return Optional.of(info);

    } catch (IOException e) {
      log.error("Failed to get file info: {}/{}", bucketName, fileName, e);
      throw e;
    }
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

  // ==================== Private Methods ====================

  /**
   * Return the file location following the {@link FolderStructureStrategy}
   *
   * @param filename
   * @param isDerivative
   * @return
   */
  private Path getFilePath(String bucket, String filename, boolean isDerivative) {
    return rootPath.resolve(bucket)
      .resolve(folderStructureStrategy.getPathFor(filename, isDerivative));
  }

  /**
   * Writes an InputStream to a file on an NFS mount with strict data integrity guarantees.
   * <p>
   * This method employs a "Write-Temp-Sync-Move" strategy to overcome standard NFS
   * caching and latency issues:
   * <ol>
   *   <li>Creates a unique temporary file in the same directory as the target.</li>
   *   <li>Writes stream data to the temporary file.</li>
   *   <li>Forces a physical disk sync (NFS COMMIT) using {@code FileChannel.force(true)}.</li>
   *   <li>Atomically renames the temporary file to the final destination.</li>
   * </ol>
   * This ensures that the destination file is never in a partial or corrupted state
   * visible to other clients, even if the application crashes or the network fails
   * during the write operation.
   * </p>
   * <p>
   * <b>Note:</b> This implementation was assisted by AI.
   * </p>
   *
   * @param targetFile The absolute or relative path to the destination file.
   *                   Must have a valid parent directory.
   * @param input      The source InputStream to read from.
   * @throws IOException If the parent directory is invalid, the disk is full, the
   *                     network fails, or the NFS server rejects the commit.
   */
  private void saveFileNFS(Path targetFile, InputStream input) throws IOException {

    String fileName = targetFile.getFileName().toString();
    Path targetDir = targetFile.getParent();

    // VALIDATION: Throw explicit exception if parent is null.
    // This happens if 'targetFile' is just a filename ("file.txt") or root ("/").
    if (targetDir == null) {
      throw new IOException("Invalid path: Target file must be in a directory, but got: " + targetFile);
    }

    // Ensure the directory structure exists before we try to write to it.
    Files.createDirectories(targetDir);

    // 1. Setup paths
    // We use a temp file in the SAME directory to ensure the final move is
    // a cheap "rename" operation rather than a slow "copy".
    Path tempPath = targetDir.resolve("." + fileName + "." + UUID.randomUUID() + ".tmp");

    try {
      // 2. Open Temp File Channel
      try (FileChannel ch = FileChannel.open(tempPath,
        StandardOpenOption.WRITE,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING)) {

        // 3. Efficient Stream Copy
        // Allocate a buffer (8KB is standard, 16-64KB is better for high-latency NFS)
        ByteBuffer buffer = ByteBuffer.allocate(16 * 1024);
        byte[] rawBuffer = buffer.array();
        int bytesRead;

        while ((bytesRead = input.read(rawBuffer)) != -1) {
          buffer.limit(bytesRead);
          buffer.position(0);

          // Ensure the channel fully writes the buffer
          while (buffer.hasRemaining()) {
            ch.write(buffer);
          }
          buffer.clear();
        }

        // 4. CRITICAL: Force Sync
        // 'true' ensures file CONTENT + METADATA (size) are on the server disk.
        // If the server is down or disk is full, this throws IOException here.
        ch.force(true);
      }

      // 5. Atomic Move
      // This swaps the file pointer. The user will see either the old file
      // or the totally complete new file. Never a partial file.
      Files.move(tempPath, targetFile,
        StandardCopyOption.ATOMIC_MOVE,
        StandardCopyOption.REPLACE_EXISTING);

    } catch (IOException e) {
      // Cleanup: If anything fails (disk full, network cut), delete the garbage temp file.
      try { Files.deleteIfExists(tempPath); } catch (IOException ignored) {}
      throw e; // Re-throw so the caller knows it failed.
    }
  }
}
