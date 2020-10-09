package ca.gc.aafc.objectstore.api.minio;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.google.common.collect.Streams;

import org.springframework.stereotype.Service;

import ca.gc.aafc.objectstore.api.file.FileInformationService;
import ca.gc.aafc.objectstore.api.file.FileObjectInfo;
import ca.gc.aafc.objectstore.api.file.FolderStructureStrategy;
import io.minio.ErrorCode;
import io.minio.MinioClient;
import io.minio.ObjectStat;
import io.minio.PutObjectOptions;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.RegionConflictException;
import io.minio.errors.XmlParserException;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class MinioFileService implements FileInformationService {

  private static final int UNKNOWN_OBJECT_SIZE = -1;
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
    return Streams.stream(path.iterator()).map(p -> p.getFileName().toString())
        .collect(Collectors.joining("/"));
  }

  /**
   * Return the file location following the {@link FolderStructureStrategy}
   * 
   * @param filename
   * @return
   */
  private String getFileLocation(String filename) {
    return toMinioObjectName(folderStructureStrategy.getPathFor(filename));
  }

  private static boolean isNotFoundException(ErrorResponseException erEx) {
    return ErrorCode.NO_SUCH_KEY == erEx.errorResponse().errorCode()
        || ErrorCode.NO_SUCH_OBJECT == erEx.errorResponse().errorCode()
        || ErrorCode.NO_SUCH_BUCKET == erEx.errorResponse().errorCode();
  }

  /**
   * * Store a file (received as an InputStream) on Minio into a specific bucket.
   * The bucket is expected to exist.
   * 
   * @param fileName
   *                     filename to be used in Minio
   * @param iStream
   *                     inputstream to send to Minio (won't be closed)
   * @param bucket
   *                     name of the bucket (will NOT be created if doesn't exist)
   * @param headersMap
   *                     optional, null if none
   * @throws InvalidKeyException
   * @throws ErrorResponseException
   * @throws IllegalArgumentException
   * @throws InsufficientDataException
   * @throws InternalException
   * @throws InvalidBucketNameException
   * @throws InvalidResponseException
   * @throws NoSuchAlgorithmException
   * @throws XmlParserException
   * @throws IOException
   */
  public void storeFile(String fileName, InputStream iStream, String contentType, String bucket,
      Map<String, String> headersMap)
      throws InvalidKeyException, ErrorResponseException, IllegalArgumentException,
      InsufficientDataException, InternalException, InvalidBucketNameException,
      InvalidResponseException, NoSuchAlgorithmException, XmlParserException, IOException {

    PutObjectOptions putObjectOptions = createPutOptions(iStream.available(), contentType, headersMap);
    minioClient.putObject(bucket, getFileLocation(fileName), iStream, putObjectOptions);
  }

  private PutObjectOptions createPutOptions(
    int availableBytes,
    String contentType,
    Map<String, String> headersMap
  ) {
    long partSize = availableBytes * 1L;

    if (partSize > PutObjectOptions.MAX_PART_SIZE) {
      partSize = PutObjectOptions.MAX_PART_SIZE;
    } else if (partSize < PutObjectOptions.MIN_MULTIPART_SIZE) {
      partSize = PutObjectOptions.MIN_MULTIPART_SIZE;
    }

    PutObjectOptions putObjectOptions = new PutObjectOptions(UNKNOWN_OBJECT_SIZE, partSize);
    putObjectOptions.setContentType(contentType);
    putObjectOptions.setHeaders(headersMap);
    return putObjectOptions;
  }

  public void ensureBucketExists(String bucketName) throws IOException {
    try {
      if (!minioClient.bucketExists(bucketName)) {
        minioClient.makeBucket(bucketName);
      }
    } catch (InvalidKeyException | ErrorResponseException | IllegalArgumentException
        | InsufficientDataException | InternalException
        | InvalidResponseException | NoSuchAlgorithmException | RegionConflictException
        | XmlParserException e) {
      throw new IOException(e);
    } catch (InvalidBucketNameException ibnEx) {
      throw new IllegalStateException(ibnEx);
    }
  }

  @Override
  public boolean bucketExists(String bucketName) {
    try {
      return minioClient.bucketExists(bucketName);
    } catch (InvalidKeyException | ErrorResponseException | IllegalArgumentException
        | InsufficientDataException | InternalException | InvalidBucketNameException
        | InvalidResponseException | NoSuchAlgorithmException | XmlParserException
        | IOException e) {
      log.info("bucketExists exception", e);
    }
    return false;
  }

  public Optional<InputStream> getFile(String fileName, String bucketName) throws IOException {
    try {
      return Optional.of(minioClient.getObject(bucketName, getFileLocation(fileName)));
    } catch (ErrorResponseException erEx) {
      if (isNotFoundException(erEx)) {
        return Optional.empty();
      }
      throw new IOException(erEx);
    } catch (InvalidKeyException | IllegalArgumentException | InsufficientDataException
        | InternalException | InvalidBucketNameException | InvalidResponseException
        | NoSuchAlgorithmException | XmlParserException e) {
      throw new IOException(e);
    }
  }

  /**
   * See {@link FileInformationService#getFileInfo(String, String)}
   */
  public Optional<FileObjectInfo> getFileInfo(String fileName, String bucketName) throws IOException {
    ObjectStat objectStat;
    try {
      objectStat = minioClient.statObject(bucketName, getFileLocation(fileName));
      
      return Optional.of(FileObjectInfo.builder()
          .length(objectStat.length())
          .contentType(objectStat.contentType())
          .headerMap(objectStat.httpHeaders())
          .build());
    } catch (ErrorResponseException erEx) {
      if (ErrorCode.NO_SUCH_KEY == erEx.errorResponse().errorCode()
          || ErrorCode.NO_SUCH_BUCKET == erEx.errorResponse().errorCode()) {
        log.debug("file: {}, bucket: {} : not found", () -> fileName, () -> bucketName);
        return Optional.empty();
      }
      throw new IOException(erEx);
    } catch (InvalidKeyException | IllegalArgumentException | InsufficientDataException
        | InternalException | InvalidBucketNameException | InvalidResponseException
        | NoSuchAlgorithmException | XmlParserException e) {
      throw new IOException(e);
    }
  }

}
