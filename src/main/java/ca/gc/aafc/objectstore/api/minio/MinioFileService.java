package ca.gc.aafc.objectstore.api.minio;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;
import org.xmlpull.v1.XmlPullParserException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Streams;

import ca.gc.aafc.objectstore.api.file.FileInformationService;
import ca.gc.aafc.objectstore.api.file.FileObjectInfo;
import ca.gc.aafc.objectstore.api.file.FolderStructureStrategy;
import io.minio.ErrorCode;
import io.minio.MinioClient;
import io.minio.ObjectStat;
import io.minio.Result;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidArgumentException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.NoResponseException;
import io.minio.errors.RegionConflictException;
import io.minio.messages.Item;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class MinioFileService implements FileInformationService {

  private final MinioClient minioClient;
  private final FolderStructureStrategy folderStructureStrategy;
  private final ObjectMapper objectMapper;
  
  @Inject
  public MinioFileService(MinioClient minioClient, FolderStructureStrategy folderStructureStrategy,
      Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder) {
    this.minioClient = minioClient;
    this.folderStructureStrategy = folderStructureStrategy;
    this.objectMapper = jackson2ObjectMapperBuilder.build();
  }
  
  /**
   * Utility method that can turn a {@link Path} into a Minio object name. {@link Path} is different
   * depending on the OS but the Minio object name will always be the same.
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
   * Store a file (received as an InputStream) on Minio into a specific bucket.
   * The bucket is expected to exist.
   * 
   * @param fileName filename to be used in Minio
   * @param iStream inputstream to send to Minio (won't be closed)
   * @param bucket name of the bucket (will NOT be created if doesn't exist)
   * @param headersMap optional, null if none
   * @throws NoSuchAlgorithmException
   * @throws IOException
   * @throws InvalidKeyException
   * @throws InvalidBucketNameException
   * @throws NoResponseException
   * @throws ErrorResponseException
   * @throws InternalException
   * @throws InvalidArgumentException
   * @throws InsufficientDataException
   * @throws InvalidResponseException
   * @throws XmlPullParserException
   * @throws RegionConflictException
   * @throws InvalidEndpointException
   * @throws InvalidPortException
   * @throws URISyntaxException
   */
  public void storeFile(String fileName, InputStream iStream, String contentType, String bucket, Map<String, String> headersMap)
      throws NoSuchAlgorithmException, IOException, InvalidKeyException, InvalidBucketNameException,
      NoResponseException, ErrorResponseException, InternalException, InvalidArgumentException,
      InsufficientDataException, InvalidResponseException, XmlPullParserException,
      RegionConflictException, InvalidEndpointException, InvalidPortException, URISyntaxException {

    // Upload the file to the bucket
    minioClient.putObject(bucket, getFileLocation(fileName), iStream, null, headersMap, null, contentType);
  }
  
  public void ensureBucketExists(String bucketName) throws IOException {
    try {
      if (!minioClient.bucketExists(bucketName)) {
        minioClient.makeBucket(bucketName);
      }
    } catch (InvalidKeyException | InvalidBucketNameException | NoSuchAlgorithmException
        | InsufficientDataException | NoResponseException | ErrorResponseException
        | InternalException | InvalidResponseException | RegionConflictException
        | XmlPullParserException e) {
      throw new IOException(e);
    }
  }
  
  @Override
  public boolean bucketExists(String bucketName) {
    try {
      return minioClient.bucketExists(bucketName);
    } catch (InvalidKeyException | InvalidBucketNameException | NoSuchAlgorithmException
        | InsufficientDataException | NoResponseException | ErrorResponseException
        | InternalException | InvalidResponseException | IOException | XmlPullParserException e) {
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
    } catch (InvalidKeyException | InvalidBucketNameException | NoSuchAlgorithmException
        | InsufficientDataException | NoResponseException | InternalException
        | InvalidArgumentException | InvalidResponseException | XmlPullParserException e) {
      throw new IOException(e);
    }
  }
  
  public <T> Optional<T> getJsonFileContentAs(String bucketName, String filename, Class<T> clazz)
      throws IOException {
    Optional<InputStream> is = getFile(filename, bucketName);
    if (!is.isPresent()) {
      return Optional.empty();
    }
    return Optional.of(objectMapper.readValue(is.get(), clazz));
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
    } catch (InvalidKeyException | InvalidBucketNameException | NoSuchAlgorithmException
        | InsufficientDataException | NoResponseException | InternalException
        | InvalidResponseException | InvalidArgumentException | XmlPullParserException e) {
      throw new IOException(e);
    }
  }
  
  /**
   * @see FileInformationService#isFileWithPrefixExists(String, String)
   * 
   */
  @Override
  public boolean isFileWithPrefixExists(String bucketName, String prefix)
      throws IllegalStateException, IOException {
    try {
      Iterator<Result<Item>> result = minioClient.listObjects(bucketName, getFileLocation(prefix))
          .iterator();
      if (result.hasNext()) {
        return true;
      }

      // when hasNext returns false it could also mean an error
      Result<Item> nextResult = result.next();
      if (nextResult != null) {
        // get will throw an exception if one occurred in the call to Minio
        nextResult.get();
      }
    } catch (NoSuchElementException e) {
      // ignore. Just a safety catch since MinioClient uses the iterator that is not exactly what
      // the interface defines.
      // next hasNext returns false but there is an element in the iterator that triggers an
      // exception
    } catch (XmlPullParserException | InvalidKeyException | InvalidBucketNameException
        | NoSuchAlgorithmException | InsufficientDataException | NoResponseException
        | ErrorResponseException | InternalException e) {
      throw new IllegalStateException(e);
    }
    return false;
  }

}
