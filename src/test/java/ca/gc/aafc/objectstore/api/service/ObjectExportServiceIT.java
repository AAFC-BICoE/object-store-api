package ca.gc.aafc.objectstore.api.service;

import org.apache.tika.mime.MimeTypeException;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.DinaAuthenticatedUserConfig;
import ca.gc.aafc.objectstore.api.config.AsyncOverrideConfig;
import ca.gc.aafc.objectstore.api.dto.ObjectUploadDto;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.file.FileController;
import ca.gc.aafc.objectstore.api.file.TemporaryObjectAccessController;
import ca.gc.aafc.objectstore.api.minio.MinioFileService;
import ca.gc.aafc.objectstore.api.minio.MinioTestContainerInitializer;
import ca.gc.aafc.objectstore.api.testsupport.factories.MultipartFileFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;

@ContextConfiguration(initializers = MinioTestContainerInitializer.class)
@Import(AsyncOverrideConfig.class)
public class ObjectExportServiceIT extends BaseIntegrationTest {

  private static final String TEST_UPLOAD_FILE_NAME = "drawing.png";

  @Inject
  private ResourceLoader resourceLoader;

  @Inject
  private FileController fileController;

  @Inject
  private MinioFileService minioFileService;

  @Inject
  private ObjectExportService objectExportService;

  @Inject
  private TemporaryObjectAccessController toaController;

  @Test
  public void endToEndMetadataServiceTest()
    throws IOException, MimeTypeException, NoSuchAlgorithmException {

    // 1 - Upload file
    MockMultipartFile mockFile = MultipartFileFactory
      .createMockMultipartFile(resourceLoader, TEST_UPLOAD_FILE_NAME, MediaType.IMAGE_PNG_VALUE);

    ObjectUploadDto uploadResponse = fileController.handleFileUpload(mockFile,
      DinaAuthenticatedUserConfig.TEST_BUCKET);
    assertNotNull(uploadResponse);
    assertNotNull(uploadResponse.getFileIdentifier());

    // 2 - Created metadata for it
    ObjectStoreMetadata osm = ObjectStoreMetadataFactory
      .newObjectStoreMetadata()
      .bucket(DinaAuthenticatedUserConfig.TEST_BUCKET)
      .fileIdentifier(uploadResponse.getFileIdentifier())
      .build();
    objectStoreMetaDataService.create(osm);

    // 3 - Make sure a thumbnail is generated
    Optional<Derivative> thumbnail = derivativeService.findThumbnailDerivativeForMetadata(osm);
    assertTrue(thumbnail.isPresent());

    var result = objectExportService.export(List.of(osm.getFileIdentifier()));

    // make sure we can get the export file using the toa key
    assertEquals(200, toaController.downloadObject(result.toaKey()).getStatusCode().value());

  }

}