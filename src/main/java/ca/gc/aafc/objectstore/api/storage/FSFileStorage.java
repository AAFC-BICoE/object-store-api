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
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;

@ConditionalOnProperty(prefix = "dina.fileStorage", name = "implementation", havingValue = "FS")
@Service
@Log4j2
public class FSFileStorage implements FileStorage {

  private final Path rootPath;
  private final boolean isNFS;

  private final FolderStructureStrategy folderStructureStrategy;

  /**
   * @param rootPath Base directory for storage (e.g., "/mnt/nfs")
   */
  public FSFileStorage(@Value("${dina.fileStorage.root}") String rootPath, FolderStructureStrategy folderStructureStrategy)
      throws IOException {
    this.rootPath = Paths.get(rootPath);
    this.folderStructureStrategy = folderStructureStrategy;

    this.isNFS = isNFS(rootPath);
    log.info("FS Storage Mode.{}", isNFS ? "(NFS)" : "");
  }

  @Override
  public void storeFile(String bucket, String fileName, boolean isDerivative,
                        String contentType, InputStream iStream) throws IOException {

    Path filePath = getFilePath(bucket, fileName, isDerivative);
    try {
      // Write stream to temp file with fsync
      saveFile(filePath, iStream);

      log.info("Stored file. Bucket:{} : {}", bucket, fileName);

    } catch (IOException e) {
     // deleteTempFile(tempFile);
      log.error("Failed to store file. Bucket:{} : {}", bucket, fileName, e);
      throw e;
    }
  }

  @Override
  public Optional<InputStream> retrieveFile(String bucket, String fileName,
                                            boolean isDerivative) throws IOException {

    Path filePath = getFilePath(bucket, fileName, isDerivative);

    if (!Files.exists(filePath)) {
      log.debug("File not found. Bucket:{} : {}", bucket, fileName);
      return Optional.empty();
    }

    try {
      InputStream is = Files.newInputStream(filePath);
      log.info("Retrieved file. Bucket:{} : {}", bucket, fileName);
      return Optional.of(is);
    } catch (IOException e) {
      log.error("Failed to retrieve file. Bucket:{} : {}", bucket, fileName, e);
      throw e;
    }
  }

  @Override
  public void deleteFile(String bucket, String fileName, boolean isDerivative)
      throws IOException {

    Path filePath = getFilePath(bucket, fileName, isDerivative);
    try {
      Files.deleteIfExists(filePath);
      log.info("Deleted file. Bucket:{} : {}", bucket, fileName);
    } catch (IOException e) {
      log.error("Failed to delete file. Bucket:{} : {}", bucket, fileName, e);
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

      FileObjectInfo info = FileObjectInfo.builder()
        .length(size)
        .contentType(contentType)
        .build();

      log.debug("Got file info. Bucket:{} : {}", bucketName, fileName);
      return Optional.of(info);

    } catch (IOException e) {
      log.error("Failed to get file info. Bucket:{} : {}", bucketName, fileName, e);
      throw e;
    }
  }

  // ==================== Private Methods ====================

  public static boolean isNFS(String folderPath) throws IOException {

    Path path = Paths.get(folderPath);

    // The path must exist to check its store
    if (!Files.exists(path)) {
      throw new IllegalArgumentException("Path does not exist");
    }

    FileStore store = Files.getFileStore(path);
    String fsType = store.type().toLowerCase();

    // Check for variations like "nfs", "nfs3", "nfs4"
    return fsType.contains("nfs");
  }

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
  private void saveFile(Path targetFile, InputStream input) throws IOException {

    Objects.requireNonNull(targetFile);

    // VALIDATION
    // targetFile.getFileName() returns null if path is root (e.g. "/").
    // targetFile.getParent() also return null if it is just a filename ("file.txt") or root ("/").
    Path targetDir = targetFile.getParent();
    Path fileName = targetFile.getFileName();

    // VALIDATION: Ensure we have both a directory to write to and a filename to write.
    if (targetDir == null || fileName == null) {
      throw new IOException("Invalid target path (missing parent or filename): " + targetFile);
    }

    // Ensure the directory structure exists before we try to write to it.
    Files.createDirectories(targetDir);

    // 1. Setup paths
    // We use a temp file in the SAME directory to ensure the final move is
    // a cheap "rename" operation rather than a slow "copy".
    Path tempPath = targetDir.resolve("." + fileName + "." + UUID.randomUUID() + ".tmp");

    try {
      // 2. Write to Temp File
      if (isNFS) {
        writeNfs(tempPath, input);
      } else {
        writeLocal(tempPath, input);
      }

      // 3. Atomic Move
      // This swaps the file pointer. The user will see either the old file
      // or the totally complete new file. Never a partial file.
      Files.move(tempPath, targetFile,
        StandardCopyOption.ATOMIC_MOVE,
        StandardCopyOption.REPLACE_EXISTING);

    } catch (IOException e) {
      // Cleanup: If anything fails (disk full, network cut), delete the garbage temp file.
      try {
        Files.deleteIfExists(tempPath);
      } catch (IOException ignored) {
      }
      throw e; // Re-throw so the caller knows it failed.
    }
  }

  private void writeLocal(Path tempPath, InputStream input) throws IOException {
    // Files.copy is highly optimized by the JVM for local disks.
    // We do NOT use force(true) here to maintain high performance.
    // The Atomic Move later handles the crash-safety aspect.
    Files.copy(input, tempPath, StandardCopyOption.REPLACE_EXISTING);
  }

  private void writeNfs(Path tempPath, InputStream input) throws IOException {
    try (FileChannel ch = FileChannel.open(tempPath,
      StandardOpenOption.WRITE,
      StandardOpenOption.CREATE,
      StandardOpenOption.TRUNCATE_EXISTING)) {

      // 16KB buffer for network latency optimization
      ByteBuffer buffer = ByteBuffer.allocate(16 * 1024);
      byte[] rawBuffer = buffer.array();
      int bytesRead;

      while ((bytesRead = input.read(rawBuffer)) != -1) {
        buffer.limit(bytesRead);
        buffer.position(0);
        while (buffer.hasRemaining()) {
          ch.write(buffer);
        }
        buffer.clear();
      }

      // CRITICAL: Force physical sync for NFS
      ch.force(true);
    }
  }
}
