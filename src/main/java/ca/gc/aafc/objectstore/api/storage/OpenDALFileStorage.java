package ca.gc.aafc.objectstore.api.storage;

import org.apache.opendal.Metadata;
import org.apache.opendal.OpenDALException;
import org.apache.opendal.Operator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import ca.gc.aafc.objectstore.api.config.S3Config;
import ca.gc.aafc.objectstore.api.file.FileObjectInfo;
import ca.gc.aafc.objectstore.api.file.FolderStructureStrategy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@ConditionalOnProperty(prefix = "dina.fileStorage", name = "implementation", havingValue = "S3")
@Service
public class OpenDALFileStorage implements FileStorage {

  private final String endpoint;
  private final String accessKey;
  private final String secretKey;

  private final FolderStructureStrategy folderStructureStrategy;

  // Cache async Operators per bucket
  private final Map<String, Operator> operatorCache = new ConcurrentHashMap<>();

  public OpenDALFileStorage( FolderStructureStrategy folderStructureStrategy, S3Config s3Config) {
    this.endpoint = s3Config.getEndpoint();
    this.accessKey = s3Config.getAccessKey();
    this.secretKey = s3Config.getSecretKey();

    this.folderStructureStrategy = folderStructureStrategy;
  }

  /**
   * Utility method that can turn a {@link Path} into a Minio object name.
   * {@link Path} is different depending on the OS but the Minio object name will
   * always be the same.
   *
   * @param path
   * @return minio object name
   */
  public static String toS3ObjectName(Path path) {
    Objects.requireNonNull(path);
    return StreamSupport.stream(path.spliterator(), false).map(p -> p.getFileName().toString())
      .collect(Collectors.joining("/"));
  }

  /**
   * Return the file location following the {@link FolderStructureStrategy}
   *
   * @param filename
   * @return
   */
  private String getFileLocation(String filename, boolean isDerivative) {
    return toS3ObjectName(folderStructureStrategy.getPathFor(filename, isDerivative));
  }

  /**
   * Retrieves an Async Operator for the bucket.
   */
  private Operator getOperator(String bucketName) {
    return operatorCache.computeIfAbsent(bucketName, bucket -> {
      Map<String, String> map = new HashMap<>();
      map.put("root", "/");
      map.put("region", "a");
      map.put("bucket", bucket);
      map.put("endpoint", endpoint);
      map.put("access_key_id", accessKey);
      map.put("secret_access_key", secretKey);

      return Operator.of("s3", map);
    });
  }

  @Override
  public void storeFile(String bucket, String fileName, boolean isDerivative, String contentType,
                        InputStream iStream) throws IOException {
    var op = getOperator(bucket);
    var path = getFileLocation(fileName, isDerivative);

    // native createOutputStream returns a Java OutputStream linked to S3
    try (OutputStream s3Out = op.createOutputStream(path)) {

      // Java 9+ method to stream data efficiently from Input to Output
      iStream.transferTo(s3Out);

    } catch (OpenDALException e) {
      throw new IOException("Failed to upload file to S3: " + e.getMessage(), e);
    }
  }

  @Override
  public Optional<InputStream> retrieveFile(String bucket, String fileName, boolean isDerivative)
    throws IOException {
    var op = getOperator(bucket);
    var path = getFileLocation(fileName, isDerivative);

    try {
      // Check if the file exists first
      op.stat(path);

      InputStream s3In = op.createInputStream(path);
      return Optional.of(s3In);

    } catch (OpenDALException e) {
      if (e.getCode() == OpenDALException.Code.NotFound) {
        return Optional.empty();
      }
      throw new IOException("Failed to open stream from S3: " + e.getMessage(), e);
    }
  }

  @Override
  public void deleteFile(String bucket, String fileName, boolean isDerivative) throws IOException {
    try {
      getOperator(bucket).delete(getFileLocation(fileName, isDerivative));
    } catch (OpenDALException e) {
      throw new IOException("Failed to delete file: " + e.getMessage(), e);
    }
  }

  @Override
  public Optional<FileObjectInfo> getFileInfo(String bucket, String fileName, boolean isDerivative)
    throws IOException {
    var op = getOperator(bucket);
    var path = getFileLocation(fileName, isDerivative);

    try {
      Metadata meta = op.stat(path);
      return Optional.of(FileObjectInfo.builder()
        .length(meta.getContentLength())
        .contentType(meta.getContentType())
        .build());
    } catch (OpenDALException e) {
      if (e.getCode() == OpenDALException.Code.NotFound) {
        return Optional.empty();
      }
      throw new IOException("Failed to get file info: " + e.getMessage(), e);
    }
  }

  @Override
  public void ensureBucketExists(String bucketName) throws IOException {
    var op = getOperator(bucketName);
    try {
      // Simple check to see if we can access the root of the bucket
      op.stat("/");
    } catch (OpenDALException e) {
      if (e.getCode() == OpenDALException.Code.NotFound) {
        throw new IOException("Bucket " + bucketName + " does not exist.");
      }
      throw new IOException("Bucket check failed: " + e.getMessage(), e);
    }
  }
}
