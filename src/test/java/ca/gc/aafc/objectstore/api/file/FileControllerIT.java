package ca.gc.aafc.objectstore.api.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.sl.usermodel.ObjectMetaData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.TransactionTemplate;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.DinaAuthenticatedUserConfig;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.minio.MinioFileService;
import ca.gc.aafc.objectstore.api.service.ObjectStoreMetaDataService;
import ca.gc.aafc.objectstore.api.service.ObjectUploadService;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import io.crnk.core.exception.UnauthorizedException;
import ca.gc.aafc.objectstore.api.MinioTestConfiguration;


@Import(MinioTestConfiguration.class)
public class FileControllerIT extends BaseIntegrationTest {
  
  @Inject
  private ResourceLoader resourceLoader;

  @Inject
  private FileController fileController;

  @Inject
  private MinioFileService minioFileService;

  @Inject
  private ObjectStoreMetaDataService objectStoreMetaDataService;

  @Inject
  private ObjectUploadService objectUploadService;
  
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

  @org.springframework.transaction.annotation.Transactional(propagation = Propagation.NEVER)
  @Test
  public void fileUpload_whenImageIsUploaded_generateThumbnail() throws Exception {
    MockMultipartFile mockFile = getFileUnderTest();
    ObjectUpload uploadResponse = fileController.handleFileUpload(mockFile, bucketUnderTest);
    UUID uploadFileId = uploadResponse.getFileIdentifier();
    UUID thumbnailIdentifier;
    String thumbnailFilename = "";
    //Wait fo the thumbnail to be generated
    for (int attempts = 0; attempts <= 10; attempts++) {
      uploadResponse = objectUploadService.findOne(uploadFileId,  ObjectUpload.class);
      thumbnailIdentifier = uploadResponse.getThumbnailIdentifier();          
      if( thumbnailIdentifier != null ){
        thumbnailFilename = thumbnailIdentifier.toString() + ".thumbnail" + ThumbnailService.THUMBNAIL_EXTENSION;
        if(minioFileService.getFile(thumbnailFilename, uploadResponse.getBucket()).isPresent()) {
          break;    
        }       
      }   

      Thread.sleep(100);
    }          
    // Persist the metadata and thumbnail meta
    transactionTemplate.execute(transactionStatus -> {
      ObjectStoreMetadata objectStoreMetaUnderTest = ObjectStoreMetadataFactory
      .newObjectStoreMetadata()
      .acMetadataCreator(UUID.randomUUID())
      .dcCreator(UUID.randomUUID())
      .fileIdentifier(uploadFileId)
      .build();
      return objectStoreMetaDataService.create(objectStoreMetaUnderTest);
    });    

    ResponseEntity<InputStreamResource> thumbnailDownloadResponse = fileController.downloadObject(
      bucketUnderTest,
      thumbnailFilename
    );
    assertEquals(HttpStatus.OK, thumbnailDownloadResponse.getStatusCode());
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
    MockMultipartFile mockFile = getMockMultipartFile("cc0_test_image.jpg", MediaType.IMAGE_JPEG_VALUE);
    ObjectUpload uploadResponse = fileController.handleFileUpload(mockFile, bucketUnderTest);
    ObjectUpload objUploaded = objectUploadService.findOne(uploadResponse.getFileIdentifier(), ObjectUpload.class);

    assertNotNull(objUploaded);
  }

  private MockMultipartFile getFileUnderTest() throws IOException {
    return getMockMultipartFile("drawing.png", MediaType.IMAGE_PNG_VALUE);
  }

  private MockMultipartFile getMockMultipartFile(String fileNameInClasspath, String mediaType) throws IOException {
    Resource imageFile = resourceLoader.getResource("classpath:" + fileNameInClasspath);
    byte[] bytes = IOUtils.toByteArray(imageFile.getInputStream());

    return new MockMultipartFile("file", "testfile", mediaType, bytes);
  }

}
