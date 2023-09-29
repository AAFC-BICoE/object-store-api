package ca.gc.aafc.objectstore.api.file;

import ca.gc.aafc.dina.testsupport.security.WithMockKeycloakUser;
import ca.gc.aafc.dina.workbook.WorkbookConverter;
import ca.gc.aafc.dina.workbook.WorkbookRow;
import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.DinaAuthenticatedUserConfig;
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
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.mime.MimeTypeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(initializers = MinioTestContainerInitializer.class)
@SpringBootTest(properties = "keycloak.enabled = true")
public class FileControllerIT extends BaseIntegrationTest {

  private static final String TEST_UPLOAD_FILE_NAME = "drawing.png";
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

  @Test
  @WithMockKeycloakUser(groupRole = DinaAuthenticatedUserConfig.TEST_BUCKET + ":USER")
  public void fileUpload_OnValidUpload_testRoundTrip() throws Exception {
    MockMultipartFile mockFile = getFileUnderTest();

    ObjectUploadDto uploadResponse = fileController.handleFileUpload(mockFile, bucketUnderTest);
    assertNotNull(uploadResponse);

    // file can only be downloaded if we attach metadata to it
    ObjectStoreMetadataDto metadataForFile = new ObjectStoreMetadataDto();
    metadataForFile.setBucket(bucketUnderTest);

    metadataForFile.setFileIdentifier(uploadResponse.getFileIdentifier());
    objectStoreResourceRepository.create(metadataForFile);

    ResponseEntity<InputStreamResource> response = fileController.downloadObject(bucketUnderTest, uploadResponse.getFileIdentifier());

    // on download, the original file name should be returned
    assertEquals(mockFile.getOriginalFilename(), response.getHeaders().getContentDisposition().getFilename());
  }

  @Test
 // @WithMockKeycloakUser(groupRole = DinaAuthenticatedUserConfig.TEST_BUCKET + ":USER")
  public void fileUploadConversion_OnValidSpreadsheet_contentReturned() throws Exception {
    MockMultipartFile mockFile = MultipartFileFactory.createMockMultipartFile(resourceLoader,"test_spreadsheet.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE);
    Map<Integer, List<WorkbookRow>> content = fileController.handleFileConversion(mockFile);
    assertFalse(content.isEmpty());
    assertFalse(content.get(0).isEmpty());
  }

  @Test
  // @WithMockKeycloakUser(groupRole = DinaAuthenticatedUserConfig.TEST_BUCKET + ":USER")
  public void fileUploadConversion_OnValidCSV_contentReturned() throws Exception {
    // use Octet Stream to amke sure the FileController will detect it's a csv
    MockMultipartFile mockFile = MultipartFileFactory.createMockMultipartFile(resourceLoader,"test_spreadsheet.csv", MediaType.APPLICATION_OCTET_STREAM_VALUE);
    Map<Integer, List<WorkbookRow>> content = fileController.handleFileConversion(mockFile);
    assertFalse(content.isEmpty());
    assertFalse(content.get(0).isEmpty());
  }

  @Test
  @WithMockKeycloakUser(groupRole = DinaAuthenticatedUserConfig.TEST_BUCKET + ":USER")
  public void fileUpload_InvalidMediaTypeExecutable_throwsIllegalArgumentException() throws Exception {
    MockMultipartFile mockFile = MultipartFileFactory.createMockMultipartFile(resourceLoader, "testExecutable", "application/x-sharedlib");

    UnsupportedMediaTypeStatusException error = assertThrows(UnsupportedMediaTypeStatusException.class, () -> fileController.handleFileUpload(mockFile, bucketUnderTest));

    String expectedMessage = "415 UNSUPPORTED_MEDIA_TYPE \"Media type application/x-sharedlib is invalid.\"";
    String actualMessage = error.getLocalizedMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  @WithMockKeycloakUser(groupRole = DinaAuthenticatedUserConfig.TEST_BUCKET + ":USER")
  public void fileUpload_InvalidMediaTypeZIP_throwsIllegalArgumentException() throws Exception {
    MockMultipartFile mockFile = MultipartFileFactory.createMockMultipartFile(resourceLoader, "test.zip", "application/zip");

    UnsupportedMediaTypeStatusException error = assertThrows(UnsupportedMediaTypeStatusException.class, () -> fileController.handleFileUpload(mockFile, bucketUnderTest));

    String expectedMessage = "415 UNSUPPORTED_MEDIA_TYPE \"Media type application/zip is invalid.\"";
    String actualMessage = error.getLocalizedMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  @WithMockKeycloakUser(groupRole = DinaAuthenticatedUserConfig.TEST_BUCKET + ":USER")
  public void fileUpload_gzipUpload_ObjectUploadEntryCreated() throws Exception {
    MockMultipartFile mockFile = MultipartFileFactory.createMockMultipartFile(resourceLoader, "testfile.txt.gz", "application/gzip");

    ObjectUploadDto uploadResponse = fileController.handleFileUpload(mockFile, bucketUnderTest);
    ObjectUpload objUploaded = objectUploadService.findOne(uploadResponse.getFileIdentifier(), ObjectUpload.class);

    assertNotNull(objUploaded);
  }

  @Test
  @WithMockKeycloakUser(groupRole = DinaAuthenticatedUserConfig.TEST_BUCKET + ":USER")
  public void fileUpload_OnValidUpload_ObjectUploadEntryCreated() throws Exception {
    MockMultipartFile mockFile = getFileUnderTest();
    ObjectUploadDto uploadResponse = fileController.handleFileUpload(mockFile, bucketUnderTest);
    ObjectUpload objUploaded = objectUploadService.findOne(uploadResponse.getFileIdentifier(), ObjectUpload.class);
    assertEquals(TEST_UPLOAD_FILE_SHA1HEX, uploadResponse.getSha1Hex());

    assertNotNull(objUploaded);
    assertNotNull(objUploaded.getDcType());
    assertTrue(StringUtils.isNotBlank(objUploaded.getCreatedBy()));
  }

  @Test
  @WithMockKeycloakUser(groupRole = DinaAuthenticatedUserConfig.TEST_BUCKET + ":USER")
  public void upload_UnAuthorizedBucket_ThrowsUnauthorizedException() throws IOException {
    MockMultipartFile mockFile = getFileUnderTest();

    assertThrows(AccessDeniedException.class,
      () -> fileController.handleFileUpload(mockFile, "ivalid-bucket"));
  }

  @Test
  @WithMockKeycloakUser(groupRole = DinaAuthenticatedUserConfig.TEST_BUCKET + ":USER")
  public void fileUpload_SameSha1Hex_ObjectUploadEntryCreatedWithWarning() throws Exception {
    MockMultipartFile mockFile = getFileUnderTest();
    MockMultipartFile sameMockFile = getFileUnderTest();

    fileController.handleFileUpload(mockFile, bucketUnderTest);
    ObjectUploadDto sameUploadResponse = fileController.handleFileUpload(sameMockFile, bucketUnderTest);

    String expectedKey = "duplicate_found";
    String expectedValue = "A file with the same content already exists";

    assertNotNull(sameUploadResponse);
    assertEquals(expectedValue, sameUploadResponse.getMeta().getWarnings().get(expectedKey));
  }

  /**
   * Test with a larger image that will exceed the read ahead buffer.
   * @throws Exception
   */
  @Test
  @WithMockKeycloakUser(groupRole = DinaAuthenticatedUserConfig.TEST_BUCKET + ":USER")
  public void fileUpload_OnValidLargerUpload_ObjectUploadEntryCreated() throws Exception {
    MockMultipartFile mockFile = MultipartFileFactory.createMockMultipartFile(resourceLoader,"cc0_test_image.jpg", MediaType.IMAGE_JPEG_VALUE);
    ObjectUploadDto uploadResponse = fileController.handleFileUpload(mockFile, bucketUnderTest);
    ObjectUpload objUploaded = objectUploadService.findOne(uploadResponse.getFileIdentifier(), ObjectUpload.class);

    assertNotNull(objUploaded);
  }

  @Test
  public void downloadDerivative_WhenDerivativeDoesNotExist_ThrowsNotFound() {
    assertThrows(ResponseStatusException.class,
      () -> fileController.downloadDerivative(bucketUnderTest, UUID.randomUUID()));
  }

  @Test
  @WithMockKeycloakUser(groupRole = DinaAuthenticatedUserConfig.TEST_BUCKET + ":USER")
  public void downloadDerivative() throws IOException, InvalidKeyException, NoSuchAlgorithmException,
    XmlParserException, InvalidResponseException, ServerException, InternalException, MimeTypeException,
    InsufficientDataException, ErrorResponseException {
    MockMultipartFile mockFile = getFileUnderTest();
    ObjectUploadDto uploadResponse = fileController.handleDerivativeUpload(mockFile, bucketUnderTest);
    ObjectUpload objectUpload = ObjectUploadFactory.newObjectUpload().build();

    objectUploadService.create(objectUpload);

    // A derivative requires a Derivative record to download
    ObjectStoreMetadata acDerivedFrom = ObjectStoreMetadataFactory.newObjectStoreMetadata().fileIdentifier(objectUpload.getFileIdentifier()).build();
    objectStoreMetaDataService.create(acDerivedFrom);
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
  @WithMockKeycloakUser(groupRole = DinaAuthenticatedUserConfig.TEST_BUCKET + ":USER")
  public void derivativeUpload_OnValidUpload() throws Exception {
    MockMultipartFile mockFile = getFileUnderTest();

    ObjectUploadDto uploadResponse = fileController.handleDerivativeUpload(mockFile, bucketUnderTest);
    assertNotNull(uploadResponse);

    //Assert object upload created
    assertNotNull(objectUploadService.findOne(uploadResponse.getFileIdentifier(), ObjectUpload.class));
  }

  private MockMultipartFile getFileUnderTest() throws IOException {
    return MultipartFileFactory.createMockMultipartFile(resourceLoader, TEST_UPLOAD_FILE_NAME, MediaType.IMAGE_PNG_VALUE);
  }


}
