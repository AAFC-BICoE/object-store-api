package ca.gc.aafc.objectstore.api.file;

import ca.gc.aafc.dina.testsupport.security.WithMockKeycloakUser;
import ca.gc.aafc.dina.util.UUIDHelper;
import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.dto.ObjectUploadDto;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.minio.MinioTestContainerInitializer;
import ca.gc.aafc.objectstore.api.repository.ObjectStoreResourceRepository;
import ca.gc.aafc.objectstore.api.testsupport.factories.MultipartFileFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.mime.MimeTypeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import javax.inject.Inject;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(initializers = MinioTestContainerInitializer.class)
@SpringBootTest(properties = "keycloak.enabled = true")
public class FileControllerIT extends BaseIntegrationTest {

  private static final String TEST_UPLOAD_FILE_NAME = "drawing.png";
  private static final String TEST_UPLOAD_FILE_EXT = "png";
  // calculated using sha1sum drawing.png
  private static final String TEST_UPLOAD_FILE_SHA1HEX = "5e51269a9f21eef93ff5fbf2e8c3ceeb3d84a430";

  @Inject
  private ResourceLoader resourceLoader;

  @Inject
  private FileController fileController;

  @Inject
  private ObjectStoreResourceRepository objectStoreResourceRepository;

  @Inject
  private TransactionTemplate transactionTemplate;

  private final static String TEST_BUCKET_NAME = "test";
  private final static String TEST_GROUP_NAME = TEST_BUCKET_NAME;

  @AfterEach
  public void cleanup() {
    // Delete the ObjectUploads that are not deleted automatically because they are
    // created
    // asynchronously outside the test's transaction:
    transactionTemplate.execute(
        transactionStatus -> {
          service.deleteByProperty(ObjectUpload.class, "bucket", TEST_BUCKET_NAME);
          return null;
        });
  }

  @Test
  @WithMockKeycloakUser(groupRole = TEST_GROUP_NAME + ":USER")
  public void fileUpload_OnValidUpload_testRoundTrip() throws Exception {
    MockMultipartFile mockFile = getFileUnderTest();

    ObjectUploadDto uploadResponse = fileController.handleFileUpload(mockFile, TEST_BUCKET_NAME);
    assertNotNull(uploadResponse);

    // file can only be downloaded if we attach metadata to it
    ObjectStoreMetadataDto metadataForFile = new ObjectStoreMetadataDto();
    metadataForFile.setBucket(TEST_BUCKET_NAME);

    metadataForFile.setFileIdentifier(uploadResponse.getFileIdentifier());
    objectStoreResourceRepository.create(metadataForFile);

    // dina-admin role required
    assertThrows(AccessDeniedException.class, () ->
      fileController.getObjectInfo(TEST_BUCKET_NAME, uploadResponse.getFileIdentifier()+ "." + TEST_UPLOAD_FILE_EXT));

    ResponseEntity<InputStreamResource> response = fileController.downloadObject(TEST_BUCKET_NAME,
        uploadResponse.getFileIdentifier());

    // on download, the original file name should be returned
    assertEquals(mockFile.getOriginalFilename(), response.getHeaders().getContentDisposition().getFilename());
  }

  @Test
  @WithMockKeycloakUser(groupRole = TEST_GROUP_NAME + ":DINA_ADMIN")
  public void fileInfo_onValidUpload_fileInfoReturned() throws Exception {
    MockMultipartFile mockFile = getFileUnderTest();

    ObjectUploadDto uploadResponse = fileController.handleFileUpload(mockFile, TEST_BUCKET_NAME);
    assertNotNull(uploadResponse);

    ResponseEntity<FileObjectInfo> foi = fileController.getObjectInfo(TEST_BUCKET_NAME, uploadResponse.getFileIdentifier()+ "." + TEST_UPLOAD_FILE_EXT);
    assertEquals(HttpStatus.OK, foi.getStatusCode());
    assertNotNull(foi.getBody());
    assertTrue(foi.getBody().getLength() > 0);

    //Test derivative
    MockMultipartFile derivativeMockFile = getFileUnderTest();
    ObjectUploadDto derivativeUploadResponse = fileController.handleDerivativeUpload(derivativeMockFile, TEST_BUCKET_NAME);
    assertNotNull(derivativeUploadResponse);

    ResponseEntity<FileObjectInfo> dfoi = fileController.getDerivativeObjectInfo(TEST_BUCKET_NAME, derivativeUploadResponse.getFileIdentifier()+ "." + TEST_UPLOAD_FILE_EXT);
    assertEquals(HttpStatus.OK, dfoi.getStatusCode());
    assertNotNull(dfoi.getBody());
    assertTrue(dfoi.getBody().getLength() > 0);

    // test non-existing file
    assertThrows(ResponseStatusException.class, () ->
      fileController.getObjectInfo(TEST_BUCKET_NAME, UUIDHelper.generateUUIDv7() + "." + TEST_UPLOAD_FILE_EXT));
  }

  @Test
  @WithMockKeycloakUser(groupRole = TEST_GROUP_NAME + ":USER")
  public void fileUpload_InvalidMediaTypeExecutable_throwsIllegalArgumentException() throws Exception {
    MockMultipartFile mockFile = MultipartFileFactory.createMockMultipartFile(resourceLoader, "testExecutable",
        "application/x-sharedlib");

    UnsupportedMediaTypeStatusException error = assertThrows(UnsupportedMediaTypeStatusException.class,
        () -> fileController.handleFileUpload(mockFile, TEST_BUCKET_NAME));

    String expectedMessage = "415 UNSUPPORTED_MEDIA_TYPE \"Media type application/x-sharedlib is invalid.\"";
    String actualMessage = error.getLocalizedMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  @WithMockKeycloakUser(groupRole = TEST_GROUP_NAME + ":USER")
  public void fileUpload_InvalidMediaTypeZIP_throwsIllegalArgumentException() throws Exception {
    MockMultipartFile mockFile = MultipartFileFactory.createMockMultipartFile(resourceLoader, "test.zip",
        "application/zip");

    UnsupportedMediaTypeStatusException error = assertThrows(UnsupportedMediaTypeStatusException.class,
        () -> fileController.handleFileUpload(mockFile, TEST_BUCKET_NAME));

    String expectedMessage = "415 UNSUPPORTED_MEDIA_TYPE \"Media type application/zip is invalid.\"";
    String actualMessage = error.getLocalizedMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  @WithMockKeycloakUser(groupRole = TEST_GROUP_NAME + ":SUPER_USER")
  public void fileUpload_gzipUpload_ObjectUploadEntryCreated() throws Exception {
    MockMultipartFile mockFile = MultipartFileFactory.createMockMultipartFile(resourceLoader, "testfile.txt.gz",
        "application/gzip");

    ObjectUploadDto uploadResponse = fileController.handleFileUpload(mockFile, TEST_BUCKET_NAME);
    ObjectUpload objUploaded = objectUploadService.findOne(uploadResponse.getFileIdentifier(), ObjectUpload.class);
    assertNotNull(objUploaded);
    fileController.getObjectInfo(TEST_BUCKET_NAME, objUploaded.getUuid() + ".gz");
  }

  @Test
  @WithMockKeycloakUser(groupRole = TEST_GROUP_NAME + ":SUPER_USER")
  public void fileUpload_emptyFile_ExceptionThrown() throws Exception {
    MockMultipartFile mockFile = new MockMultipartFile("file", "testfile.txt" , MediaType.TEXT_PLAIN_VALUE, new byte[]{});
    assertThrows(IllegalStateException.class,
      () -> fileController.handleFileUpload(mockFile, TEST_BUCKET_NAME));
  }

  @Test
  @WithMockKeycloakUser(groupRole = TEST_GROUP_NAME + ":USER")
  public void fileUpload_OnValidUpload_ObjectUploadEntryCreated() throws Exception {
    MockMultipartFile mockFile = getFileUnderTest();
    ObjectUploadDto uploadResponse = fileController.handleFileUpload(mockFile, TEST_BUCKET_NAME);
    ObjectUpload objUploaded = objectUploadService.findOne(uploadResponse.getFileIdentifier(), ObjectUpload.class);
    assertEquals(TEST_UPLOAD_FILE_SHA1HEX, uploadResponse.getSha1Hex());

    assertNotNull(objUploaded);
    assertNotNull(objUploaded.getDcType());
    assertTrue(StringUtils.isNotBlank(objUploaded.getCreatedBy()));
  }

  @Test
  @WithMockKeycloakUser(groupRole = TEST_GROUP_NAME + ":USER")
  public void upload_UnAuthorizedBucket_ThrowsUnauthorizedException() throws IOException {
    MockMultipartFile mockFile = getFileUnderTest();

    assertThrows(AccessDeniedException.class,
        () -> fileController.handleFileUpload(mockFile, "ivalid-bucket"));
  }

  @Test
  @WithMockKeycloakUser(groupRole = TEST_GROUP_NAME + ":USER")
  public void fileUpload_SameSha1Hex_ObjectUploadEntryCreatedWithWarning() throws Exception {
    MockMultipartFile mockFile = getFileUnderTest();
    MockMultipartFile sameMockFile = getFileUnderTest();

    fileController.handleFileUpload(mockFile, TEST_BUCKET_NAME);
    ObjectUploadDto sameUploadResponse = fileController.handleFileUpload(sameMockFile, TEST_BUCKET_NAME);

    String expectedKey = "duplicate_found";
    String expectedValue = "A file with the same content already exists";

    assertNotNull(sameUploadResponse);
    assertEquals(expectedValue, sameUploadResponse.getMeta().getWarnings().get(expectedKey));
  }

  /**
   * Test with a larger image that will exceed the read ahead buffer.
   * 
   * @throws Exception
   */
  @Test
  @WithMockKeycloakUser(groupRole = TEST_GROUP_NAME + ":USER")
  public void fileUpload_OnValidLargerUpload_ObjectUploadEntryCreated() throws Exception {
    MockMultipartFile mockFile = MultipartFileFactory.createMockMultipartFile(resourceLoader, "cc0_test_image.jpg",
        MediaType.IMAGE_JPEG_VALUE);
    ObjectUploadDto uploadResponse = fileController.handleFileUpload(mockFile, TEST_BUCKET_NAME);
    ObjectUpload objUploaded = objectUploadService.findOne(uploadResponse.getFileIdentifier(), ObjectUpload.class);

    assertNotNull(objUploaded);
  }

  @Test
  public void downloadDerivative_WhenDerivativeDoesNotExist_ThrowsNotFound() {
    assertThrows(ResponseStatusException.class,
        () -> fileController.downloadDerivative(TEST_BUCKET_NAME, UUIDHelper.generateUUIDv7()));
  }

  @Test
  @WithMockKeycloakUser(groupRole = TEST_GROUP_NAME + ":USER")
  public void downloadDerivative() throws IOException, NoSuchAlgorithmException, MimeTypeException{
    MockMultipartFile mockFile = getFileUnderTest();
    ObjectUploadDto uploadResponse = fileController.handleDerivativeUpload(mockFile, TEST_BUCKET_NAME);
    ObjectUpload objectUpload = ObjectUploadFactory.newObjectUpload().build();

    objectUploadService.create(objectUpload);

    // A derivative requires a Derivative record to download
    ObjectStoreMetadata acDerivedFrom = ObjectStoreMetadataFactory.newObjectStoreMetadata()
        .fileIdentifier(objectUpload.getFileIdentifier()).build();
    objectStoreMetaDataService.create(acDerivedFrom);
    Derivative derivative = derivativeService.create(Derivative.builder()
        .fileIdentifier(uploadResponse.getFileIdentifier())
        .acDerivedFrom(acDerivedFrom)
        .dcType(DcType.IMAGE)
        .createdBy("dina")
        .build());

    ResponseEntity<InputStreamResource> result = fileController.downloadDerivative(
      TEST_BUCKET_NAME,
        uploadResponse.getFileIdentifier());
    // Assert Response
    assertEquals(200, result.getStatusCode().value());
    // Assert File Content
    InputStreamResource body = result.getBody();
    assertNotNull(body);
    assertTrue(IOUtils.contentEquals(mockFile.getInputStream(), body.getInputStream()));
    
    // Assert no download permissions - wrong bucket and not publicly releasable
    derivative.setPubliclyReleasable(false);
    derivative.setBucket("abc");

    // update the record outside the service to skip validation since changing the bucket is not
    // allowed by the service
    service.save(derivative, false);

    assertThrows(AccessDeniedException.class,
        () -> fileController.downloadDerivative(
          TEST_BUCKET_NAME,
            uploadResponse.getFileIdentifier()));

    // Assert can download - wrong bucket but publicly releasable
    derivative.setPubliclyReleasable(true);
    derivativeService.update(derivative);
    ResponseEntity<InputStreamResource> response = fileController.downloadDerivative(
      TEST_BUCKET_NAME,
        uploadResponse.getFileIdentifier());
    // expected to work since (publiclyReleasable)
    assertEquals(200, response.getStatusCode().value());
  }

  @Test
  @WithMockKeycloakUser(groupRole = TEST_GROUP_NAME + ":USER")
  public void derivativeUpload_OnValidUpload() throws Exception {
    MockMultipartFile mockFile = getFileUnderTest();

    ObjectUploadDto uploadResponse = fileController.handleDerivativeUpload(mockFile, TEST_BUCKET_NAME);
    assertNotNull(uploadResponse);

    // Assert object upload created
    assertNotNull(objectUploadService.findOne(uploadResponse.getFileIdentifier(), ObjectUpload.class));
  }

  @Test
  @WithMockKeycloakUser(groupRole = TEST_GROUP_NAME + ":USER")
  public void fileDownload_onUnauthorized() throws Exception {
    MockMultipartFile mockFile = getFileUnderTest();

    ObjectUploadDto uploadResponse = fileController.handleFileUpload(mockFile, TEST_BUCKET_NAME);
    assertNotNull(uploadResponse);

    // file can only be downloaded if we attach metadata to it
    ObjectStoreMetadataDto metadataForFile = new ObjectStoreMetadataDto();
    metadataForFile.setBucket(TEST_BUCKET_NAME);

    metadataForFile.setFileIdentifier(uploadResponse.getFileIdentifier());
    metadataForFile = objectStoreResourceRepository.create(metadataForFile);

    // change the bucket using the service to avoid permission issues but set it
    // publiclyReleasable
    ObjectStoreMetadata metadataEntity = objectStoreMetaDataService.findOne(metadataForFile.getUuid());
    metadataEntity.setPubliclyReleasable(true);
    metadataEntity.setBucket("abc");
    objectStoreMetaDataService.update(metadataEntity);
    ResponseEntity<InputStreamResource> response = fileController.downloadObject(TEST_BUCKET_NAME,
        uploadResponse.getFileIdentifier());
    // expected to work since (publiclyReleasable)
    assertEquals(200, response.getStatusCode().value());

    metadataEntity.setPubliclyReleasable(false);
    objectStoreMetaDataService.update(metadataEntity);
    assertThrows(AccessDeniedException.class,
        () -> fileController.downloadObject(TEST_BUCKET_NAME, uploadResponse.getFileIdentifier()));
  }

  private MockMultipartFile getFileUnderTest() throws IOException {
    return MultipartFileFactory.createMockMultipartFile(resourceLoader, TEST_UPLOAD_FILE_NAME,
        MediaType.IMAGE_PNG_VALUE);
  }

}
