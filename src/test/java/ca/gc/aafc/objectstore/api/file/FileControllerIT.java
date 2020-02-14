package ca.gc.aafc.objectstore.api.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

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

import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.minio.MinioFileService;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;

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

  @Transactional
  @Test
  public void fileUpload_whenImageIsUploaded_generateThumbnail() throws Exception {
    Resource imageFile = resourceLoader.getResource("classpath:drawing.png");
    byte[] bytes = IOUtils.toByteArray(imageFile.getInputStream());

    MockMultipartFile mockFile = new MockMultipartFile("file", "testfile", MediaType.IMAGE_PNG_VALUE, bytes);

    FileMetaEntry uploadResponse = fileController.handleFileUpload(mockFile, "mybucket");

    UUID fileId = uploadResponse.getFileIdentifier();

    // Persist the associated metadata separately:
    ObjectStoreMetadata newMetadata = ObjectStoreMetadataFactory.newObjectStoreMetadata()
      .fileIdentifier(fileId)
      .build();
    entityManager.persist(newMetadata);

    ResponseEntity<InputStreamResource> thumbnailDownloadResponse = fileController.downloadObject(
      "mybucket",
      fileId + ".thumbnail"
    );

    assertEquals(HttpStatus.OK, thumbnailDownloadResponse.getStatusCode());
  }

  @Transactional
  @Test
  public void fileUpload_OnValidUpload_FileMetaEntryGenerated() throws Exception {
    Resource imageFile = resourceLoader.getResource("classpath:drawing.png");
    byte[] bytes = IOUtils.toByteArray(imageFile.getInputStream());

    MockMultipartFile mockFile = new MockMultipartFile("file", "testfile", MediaType.IMAGE_PNG_VALUE, bytes);

    FileMetaEntry uploadResponse = fileController.handleFileUpload(mockFile, "mybucket");

    Optional<InputStream> response = minioFileService.getFile(
      uploadResponse.getFileMetaEntryFilename(),
      "mybucket"
    );

    assertTrue(response.isPresent());
  }

}