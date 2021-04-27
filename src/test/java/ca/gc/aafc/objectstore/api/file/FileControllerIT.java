package ca.gc.aafc.objectstore.api.file;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.DinaAuthenticatedUserConfig;
import ca.gc.aafc.objectstore.api.MinioTestConfiguration;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.service.DerivativeService;
import ca.gc.aafc.objectstore.api.service.ObjectUploadService;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import io.crnk.core.exception.UnauthorizedException;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.mime.MimeTypeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Import(MinioTestConfiguration.class)
public class FileControllerIT extends BaseIntegrationTest {

  @Inject
  private ResourceLoader resourceLoader;

  @Inject
  private FileController fileController;

  @Inject
  private ObjectUploadService objectUploadService;


  @Inject
  private DerivativeService derivativeService;

  @Inject
  private TransactionTemplate transactionTemplate;

  private final static String bucketUnderTest = DinaAuthenticatedUserConfig.ROLES_PER_GROUPS.keySet().stream()
    .findFirst().get();

  @AfterEach
  public void cleanup() {
    // Delete the ObjectUploads that are not deleted automatically because they are created
    // asynchronously outside the test's transaction:
    transactionTemplate.execute(
      transactionStatus -> {
        service.deleteByProperty(ObjectUpload.class, "bucket", bucketUnderTest);
        return null;
      });
  }

  @Transactional
  @Test
  public void fileUpload_OnValidUpload_FileMetaEntryGenerated() throws Exception {
    MockMultipartFile mockFile = getFileUnderTest();

    ObjectUpload uploadResponse = fileController.handleFileUpload(mockFile, bucketUnderTest);
    assertNotNull(uploadResponse);
  }

  @Transactional
  @Test
  public void fileUpload_OnValidUpload_ObjectUploadEntryCreated() throws Exception {
    MockMultipartFile mockFile = getFileUnderTest();
    ObjectUpload uploadResponse = fileController.handleFileUpload(mockFile, bucketUnderTest);
    ObjectUpload objUploaded = objectUploadService.findOne(uploadResponse.getFileIdentifier(), ObjectUpload.class);

    assertNotNull(objUploaded);
    assertNotNull(objUploaded.getDcType());
    assertTrue(StringUtils.isNotBlank(objUploaded.getCreatedBy()));
  }

  @Test
  public void upload_UnAuthorizedBucket_ThrowsUnauthorizedException() throws IOException {
    MockMultipartFile mockFile = getFileUnderTest();

    assertThrows(
      UnauthorizedException.class,
      () -> fileController.handleFileUpload(mockFile, "ivalid-bucket"));
  }

  /**
   * Test with a larger image that will exceed the read ahead buffer.
   * @throws Exception
   */
  @Transactional
  @Test
  public void fileUpload_OnValidLargerUpload_ObjectUploadEntryCreated() throws Exception {
    MockMultipartFile mockFile = createMockMultipartFile("cc0_test_image.jpg", MediaType.IMAGE_JPEG_VALUE);
    ObjectUpload uploadResponse = fileController.handleFileUpload(mockFile, bucketUnderTest);
    ObjectUpload objUploaded = objectUploadService.findOne(uploadResponse.getFileIdentifier(), ObjectUpload.class);

    assertNotNull(objUploaded);
  }

  @Test
  public void downloadDerivative_WhenDerivativeDoesNotExist_ThrowsNotFound() {
    assertThrows(ResponseStatusException.class,
      () -> fileController.downloadDerivative(bucketUnderTest, UUID.randomUUID()));
  }

  @Test
  public void downloadDerivative() throws IOException, InvalidKeyException, NoSuchAlgorithmException,
    XmlParserException, InvalidResponseException, ServerException, InternalException, MimeTypeException,
    InvalidBucketNameException, InsufficientDataException, ErrorResponseException {
    MockMultipartFile mockFile = getFileUnderTest();
    ObjectUpload uploadResponse = fileController.handleDerivativeUpload(mockFile, bucketUnderTest);
    // A derivative requires a Derivative record to download
    ObjectStoreMetadata acDerivedFrom = ObjectStoreMetadataFactory.newObjectStoreMetadata().build();
    this.service.save(acDerivedFrom);
    derivativeService.create(Derivative.builder()
      .fileIdentifier(uploadResponse.getFileIdentifier())
      .acDerivedFrom(acDerivedFrom)
      .bucket(uploadResponse.getBucket())
      .dcFormat(uploadResponse.getDetectedMediaType())
      .fileExtension(uploadResponse.getEvaluatedFileExtension())
      .dcType(DcType.IMAGE)
      .createdBy("dina")
      .build());

    ResponseEntity<InputStreamResource> result = fileController.downloadDerivative(
      bucketUnderTest,
      uploadResponse.getFileIdentifier());
    // Assert Response
    assertEquals(200, result.getStatusCode().value());
    // Assert File Content
    InputStreamResource body = result.getBody();
    assertNotNull(body);
    assertTrue(IOUtils.contentEquals(mockFile.getInputStream(), body.getInputStream()));
  }

  @Test
  public void derivativeUpload_OnValidUpload() throws Exception {
    MockMultipartFile mockFile = getFileUnderTest();

    ObjectUpload uploadResponse = fileController.handleDerivativeUpload(mockFile, bucketUnderTest);
    assertNotNull(uploadResponse);

    //Assert object upload created
    assertNotNull(objectUploadService.findOne(uploadResponse.getFileIdentifier(), ObjectUpload.class));
  }

  private MockMultipartFile getFileUnderTest() throws IOException {
    return createMockMultipartFile("drawing.png", MediaType.IMAGE_PNG_VALUE);
  }

  private MockMultipartFile createMockMultipartFile(
    String fileNameInClasspath,
    String mediaType
  ) throws IOException {
    Resource imageFile = resourceLoader.getResource("classpath:" + fileNameInClasspath);
    byte[] bytes = IOUtils.toByteArray(imageFile.getInputStream());

    return new MockMultipartFile("file", "testfile", mediaType, bytes);
  }
}
