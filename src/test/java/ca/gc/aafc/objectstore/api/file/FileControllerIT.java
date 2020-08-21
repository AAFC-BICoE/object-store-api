package ca.gc.aafc.objectstore.api.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.service.ObjectUploadService;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import ca.gc.aafc.objectstore.api.DinaAuthenticatedUserConfig;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.minio.MinioFileService;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import io.crnk.core.exception.UnauthorizedException;

@SpringBootTest
@ActiveProfiles("test")
public class FileControllerIT {

  @Inject
  private ResourceLoader resourceLoader;

  @Inject
  private FileController fileController;

  @Inject
  private EntityManager entityManager;

  @Inject
  private MinioFileService minioFileService;

  @Inject
  private ObjectUploadService objectUploadService;

  private final static String bucketUnderTest = DinaAuthenticatedUserConfig.ROLES_PER_GROUPS.keySet().stream()
    .findFirst().get();

  @Transactional
  @Test
  public void fileUpload_whenImageIsUploaded_generateThumbnail() throws Exception {
    MockMultipartFile mockFile = getFileUnderTest();

    FileMetaEntry uploadResponse = fileController.handleFileUpload(mockFile, bucketUnderTest);

    UUID thumbnailIdentifier = uploadResponse.getThumbnailIdentifier();

    // Persist the associated metadata and thumbnail meta separately:
    ObjectStoreMetadata thumbMetaData = ObjectStoreMetadataFactory.newObjectStoreMetadata()
      .fileIdentifier(thumbnailIdentifier)
      .build();
    entityManager.persist(thumbMetaData);
    
    ResponseEntity<InputStreamResource> thumbnailDownloadResponse = fileController.downloadObject(
      bucketUnderTest,
      thumbnailIdentifier + ".thumbnail"
    );

    assertEquals(HttpStatus.OK, thumbnailDownloadResponse.getStatusCode());
  }

  @Transactional
  @Test
  public void fileUpload_OnValidUpload_FileMetaEntryGenerated() throws Exception {
    MockMultipartFile mockFile = getFileUnderTest();

    FileMetaEntry uploadResponse = fileController.handleFileUpload(mockFile, bucketUnderTest);

    Optional<InputStream> response = minioFileService.getFile(
      uploadResponse.getFileMetaEntryFilename(),
      bucketUnderTest
    );

    assertTrue(response.isPresent());
  }

  @Transactional
  @Test
  public void fileUpload_OnValidUpload_ObjectUploadEntryCreated() throws Exception {
    MockMultipartFile mockFile = getFileUnderTest();
    FileMetaEntry uploadResponse = fileController.handleFileUpload(mockFile, bucketUnderTest);
    ObjectUpload objUploaded = objectUploadService.findOne(uploadResponse.getFileIdentifier(), ObjectUpload.class);

    assertNotNull(objUploaded);
  }

  @Test
  public void upload_UnAuthorizedBucket_ThrowsUnauthorizedException() throws IOException {
    MockMultipartFile mockFile = getFileUnderTest();

    assertThrows(
      UnauthorizedException.class,
      () -> fileController.handleFileUpload(mockFile, "ivalid-bucket"));
  }

  private MockMultipartFile getFileUnderTest() throws IOException {
    Resource imageFile = resourceLoader.getResource("classpath:drawing.png");
    byte[] bytes = IOUtils.toByteArray(imageFile.getInputStream());

    return new MockMultipartFile("file", "testfile", MediaType.IMAGE_PNG_VALUE, bytes);
  }

}