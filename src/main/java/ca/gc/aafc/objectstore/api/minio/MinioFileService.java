package ca.gc.aafc.objectstore.api.minio;

import ca.gc.aafc.objectstore.api.file.FileInformationService;
import ca.gc.aafc.objectstore.api.file.FileObjectInfo;
import ca.gc.aafc.objectstore.api.file.FolderStructureStrategy;
import ca.gc.aafc.objectstore.api.storage.FileStorage;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.MinioException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.messages.ErrorResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Log4j2
public class MinioFileService implements FileStorage, FileInformationService {

  private static final int UNKNOWN_OBJECT_SIZE = -1;
  // 10MiB
  private static final int DEFAULT_PART_SIZE = 10 * 1024 * 1024;

  private final MinioClient minioClient;
  private final FolderStructureStrategy folderStructureStrategy;

  @Inject
  public MinioFileService(MinioClient minioClient, FolderStructureStrategy folderStructureStrategy) {
    this.minioClient = minioClient;
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
  public static String toMinioObjectName(Path path) {
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
    return toMinioObjectName(folderStructureStrategy.getPathFor(filename, isDerivative));
  }

  /**
   * Store a file (received as an InputStream) on Minio into a specific bucket. The bucket is expected to
   * exist.
   * @param bucket   name of the bucket (will NOT be created if doesn't exist)
   * @param fileName filename to be used in Minio
   * @param isDerivative is the file a derivative or not ?
   * @param contentType the content type of the file to store
   * @param iStream  inputStream to send to Minio (won't be closed)
   *
   */
  @Override
  public void storeFile(String bucket, String fileName, boolean isDerivative, String contentType,
                        InputStream iStream) throws IOException, IllegalStateException {
    try {
      minioClient.putObject(PutObjectArgs.builder()
        .bucket(bucket)
        .object(getFileLocation(fileName, isDerivative))
        .stream(iStream, UNKNOWN_OBJECT_SIZE, DEFAULT_PART_SIZE)
        .contentType(contentType)
        .build());
    } catch (MinioException e) {
      throw new IOException(e);
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public Optional<InputStream> retrieveFile(String bucket, String fileName, boolean isDerivative)
      throws IOException {
    try {
      return Optional.ofNullable(
        minioClient.getObject(
          GetObjectArgs.builder()
            .bucket(bucket)
            .object(getFileLocation(fileName, isDerivative))
            .build()));
    } catch (InvalidKeyException | IllegalArgumentException |
             InsufficientDataException | InternalException | InvalidResponseException |
             NoSuchAlgorithmException | XmlParserException | ServerException erEx) {
      throw new IOException(erEx);
    } catch (ErrorResponseException e) {
      if (isNotFoundError(e.errorResponse())) {
        return Optional.empty();
      } else {
        throw new IOException(e);
      }
    }
  }

  @Override
  public void ensureBucketExists(String bucketName) throws IOException {
    try {
      if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
        minioClient.makeBucket(MakeBucketArgs.builder()
            .bucket(bucketName)
            .build());
      }
    } catch (InvalidKeyException | ErrorResponseException | IllegalArgumentException
      | InsufficientDataException | InternalException
      | InvalidResponseException | NoSuchAlgorithmException
      | XmlParserException | ServerException e) {
      throw new IOException(e);
    }
  }

  @Override
  public boolean bucketExists(String bucketName) {
    try {
      return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    } catch (InvalidKeyException | ErrorResponseException | IllegalArgumentException | InsufficientDataException
      | InternalException | InvalidResponseException | NoSuchAlgorithmException |
      XmlParserException | IOException | ServerException e) {
      log.warn("bucketExists exception", e);
    }
    return false;
  }

  @Override
  public Optional<FileObjectInfo> getFileInfo(
    String bucketName,
    String fileName,
    boolean isDerivative
  ) throws IOException {
    StatObjectResponse objectStat;
    try {
      objectStat = minioClient.statObject(
          StatObjectArgs.builder()
              .bucket(bucketName)
              .object(getFileLocation(fileName, isDerivative)).build());

      return Optional.of(FileObjectInfo.builder()
        .length(objectStat.size())
        .contentType(objectStat.contentType())
        .headerMap(objectStat.headers().toMultimap())
        .build());
    } catch (InvalidKeyException | IllegalArgumentException |
      InsufficientDataException | InternalException | InvalidResponseException |
      NoSuchAlgorithmException | XmlParserException | ServerException erEx) {
      throw new IOException(erEx);
    } catch (ErrorResponseException e) {
      if (isNotFoundError(e.errorResponse())) {
        return Optional.empty();
      } else {
        throw new IOException(e);
      }
    }
  }

  @Override
  public void deleteFile(String bucket, String fileName, boolean isDerivative) throws IOException {
    try {
      minioClient.removeObject(RemoveObjectArgs.builder()
        .bucket(bucket)
        .object(getFileLocation(fileName, isDerivative))
        .build());
    } catch (ErrorResponseException | InsufficientDataException
      | InternalException | InvalidKeyException | InvalidResponseException |
      NoSuchAlgorithmException | ServerException | XmlParserException e) {
      throw new IOException(e);
    }
  }

  private static boolean isNotFoundError(ErrorResponse errorResponse) {
    if (errorResponse == null) {
      return false;
    }

    Optional<S3ErrorCode> errorCode = S3ErrorCode.fromErrorCode(errorResponse.code());
    if (errorCode.isPresent()) {
      switch (errorCode.get()) {
        case NO_SUCH_KEY:
        case NO_SUCH_BUCKET:
          return true;
        default:
      }
    }
    return false;
  }
}
