package ca.gc.aafc.objectstore.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;
import com.google.common.collect.ImmutableMap;

import io.minio.BucketExistsArgs;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.errors.ServerException;
import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;

import ca.gc.aafc.objectstore.api.file.FolderStructureStrategy;
import ca.gc.aafc.objectstore.api.minio.MinioFileService;
import io.minio.MinioClient;
import io.minio.ObjectStat;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.XmlParserException;
import okhttp3.Headers;

/**
 * 
 * Configuration used to override bean in the context of Integration testing.
 * A MinioClient stub with 1 entry will be created for testing purpose (see {@link #setupFile(MinioClient)})
 *
 */
@org.springframework.boot.test.context.TestConfiguration
public class MinioTestConfiguration {

  private final FolderStructureStrategy folderStructureStrategy = new FolderStructureStrategy();
  public static final String TEST_BUCKET = "test";
  public static final String TEST_USAGE_TERMS = "test usage terms";

  public static final UUID TEST_FILE_IDENTIFIER = UUID.randomUUID();
  public static final UUID TEST_THUMBNAIL_IDENTIFIER = UUID.randomUUID();
  public static final String TEST_FILE_EXT = ".txt";
  public static final String TEST_FILE_MEDIA_TYPE = MediaType.TEXT_PLAIN_VALUE;
  public static final String TEST_ORIGINAL_FILENAME = "myfile" + TEST_FILE_EXT;
  public static final String ILLEGAL_BUCKET_CHAR = "~";

  /**
   * Bu the ObjectUpload matching the one stored in the mock Minio.
   * @return
   */
  public static ObjectUpload buildTestObjectUpload() {
    return ObjectUploadFactory.newObjectUpload()
        .fileIdentifier(MinioTestConfiguration.TEST_FILE_IDENTIFIER)
        .thumbnailIdentifier(MinioTestConfiguration.TEST_THUMBNAIL_IDENTIFIER)
        .evaluatedMediaType(MinioTestConfiguration.TEST_FILE_MEDIA_TYPE)
        .detectedMediaType(MinioTestConfiguration.TEST_FILE_MEDIA_TYPE)
        .detectedFileExtension(MinioTestConfiguration.TEST_FILE_EXT)
        .evaluatedFileExtension(MinioTestConfiguration.TEST_FILE_EXT)
        .bucket(MinioTestConfiguration.TEST_BUCKET)
        .originalFilename(MinioTestConfiguration.TEST_ORIGINAL_FILENAME)
        .build();
  }
  
  @Primary
  @Bean
  public MinioClient initMinioClient() {
    try {
      MinioClient minioClient = new MinioClientStub();
      setupFile(minioClient);
      return minioClient;
    } catch (InvalidKeyException | InvalidBucketNameException | NoSuchAlgorithmException | ErrorResponseException |
        InternalException | InsufficientDataException | InvalidResponseException | IOException |
        InvalidEndpointException | InvalidPortException | IllegalArgumentException | XmlParserException |
        ServerException e) {
      throw new RuntimeException("Can't setup Minio client for testing", e);
    }
  }
  
  private void setupFile(MinioClient minioClient) throws InvalidKeyException,
      InvalidBucketNameException, NoSuchAlgorithmException, ErrorResponseException,
      InternalException, InsufficientDataException, InvalidResponseException, IOException,
      IllegalArgumentException, XmlParserException, ServerException {

    String testFile = "This is a test\n";
    InputStream is = new ByteArrayInputStream(
        testFile.getBytes(StandardCharsets.UTF_8));
    
    storeTestObject(minioClient, TEST_FILE_IDENTIFIER, TEST_FILE_EXT, is);
  }
  
  /**
   * Store a test object using the provided minio client.
   * 
   * @param minioClient
   * @param id
   * @param objExt
   * @param objStream
   * @throws InvalidKeyException
   * @throws InvalidBucketNameException
   * @throws NoSuchAlgorithmException
   * @throws ErrorResponseException
   * @throws InternalException
   * @throws InsufficientDataException
   * @throws InvalidResponseException
   * @throws IOException
   * @throws XmlParserException
   * @throws IllegalArgumentException
   */
  private void storeTestObject(MinioClient minioClient, UUID id, String objExt,
      InputStream objStream) throws InvalidKeyException,
      InvalidBucketNameException, NoSuchAlgorithmException, ErrorResponseException,
      InternalException, InsufficientDataException, InvalidResponseException, IOException,
      IllegalArgumentException, XmlParserException, ServerException {
    minioClient.putObject(
        PutObjectArgs.builder()
        .bucket(TEST_BUCKET)
        .object(MinioFileService.toMinioObjectName(folderStructureStrategy.getPathFor(id + objExt)))
        .stream(objStream, -1, PutObjectArgs.MAX_PART_SIZE)
    .build());
  }
  
  /**
   * Stub used to replace MinioClient for testing.
   *
   */
  public static class MinioClientStub extends MinioClient {
    
    private final Map<String, byte[]> INTERNAL_OBJECTS = new HashMap<>();

    public MinioClientStub() throws InvalidEndpointException, InvalidPortException {
      super("localhost");
    }
    
    @Override
    public boolean bucketExists(BucketExistsArgs bucketArgs){
      return true;
    }
    
    @Override
    public ObjectWriteResponse putObject(PutObjectArgs args) {
    //String bucketName, String objectName, InputStream stream, PutObjectOptions options) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      try {
        IOUtils.copy(args.stream(), baos);
      } catch (IOException e) {
        e.printStackTrace();
      }
      INTERNAL_OBJECTS.put(args.bucket() + args.object(), baos.toByteArray());
      return null;
    }
    
    @Override
    public InputStream getObject(String bucketName, String objectName) {
      return new ByteArrayInputStream(INTERNAL_OBJECTS.get(bucketName + objectName));
    }
    
//    /**
//     * If {@link MinioTestConfiguration#ILLEGAL_BUCKET_CHAR} is present in the bucket name, {@link InvalidBucketNameException}
//     * will be thrown. Otherwise, the item for {@link MinioTestConfiguration#TEST_FILE_IDENTIFIER} will be returned.
//     */
//    @Override
//    public Iterable<Result<Item>> listObjects(String bucketName, String prefix) {
//      // Trying to mimic what Minio Java SDK will do.
//      Iterator<Result<Item>> iterator = null;
//        Result<Item> result;
//        if(bucketName.contains(ILLEGAL_BUCKET_CHAR)) {
//          result = new Result<Item>(new InvalidBucketNameException(bucketName, "generated for testing purpose"));
//          iterator = new Iterator<Result<Item>>() {
//
//            @Override
//            public boolean hasNext() {
//              return false;
//            }
//
//            @Override
//            public Result<Item> next() {
//              return result;
//            }
//          };
//        }
//        else {
//          Optional<String> potentialKey = INTERNAL_OBJECTS.keySet().stream()
//              .filter(key -> key.startsWith(prefix)).findFirst();
//          if (potentialKey.isPresent()) {
//            Item item = new Item(potentialKey.get());
//            result = new Result<Item>(item);
//            iterator = Collections.singletonList(result).iterator();
//          } else {
//            iterator = Collections.emptyIterator();
//          }
//        }
//
//        final Iterator<Result<Item>> finalIterator = iterator;
//        return () -> finalIterator;
//      }

      @Override
      public ObjectStat statObject(String bucketName, String objectName) {
        Headers head = Headers.of(
          ImmutableMap.of(
            "Content-Type", TEST_FILE_MEDIA_TYPE,
            "Last-Modified", "Tue, 15 Nov 1994 12:45:26 GMT",
            "Content-Length","1234"));
        return new ObjectStat(bucketName, objectName, head);
      }
    }

}
