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
import ca.gc.aafc.objectstore.api.minio.MinioFileService;
import ca.gc.aafc.objectstore.api.minio.MinioTestContainerInitializer;
import ca.gc.aafc.objectstore.api.testsupport.factories.MultipartFileFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import javax.inject.Inject;

@ContextConfiguration(initializers = MinioTestContainerInitializer.class)
@Import(AsyncOverrideConfig.class)
public class MetadataServiceIT extends BaseIntegrationTest {

  private static final String TEST_UPLOAD_FILE_NAME = "drawing.png";

  @Inject
  private ResourceLoader resourceLoader;

  @Inject
  private FileController fileController;

  @Inject
  private MinioFileService minioFileService;

  @Test
  public void endToEndMetadataServiceTest()
    throws IOException, ServerException, MimeTypeException, InsufficientDataException,
    ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, XmlParserException,
    InvalidResponseException, InternalException {

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

    // 4 - Make sure we can load the file
    String thumbnailFilename = thumbnail.get().getFileIdentifier() + thumbnail.get().getFileExtension();
    Optional<InputStream> file = minioFileService.getFile(thumbnailFilename, DinaAuthenticatedUserConfig.TEST_BUCKET, true);
    assertTrue(file.isPresent());

    // Deleting the metadata should also delete the derivative and the system generated thumbnail
    objectStoreMetaDataService.delete(osm);
    file = minioFileService.getFile(thumbnailFilename, DinaAuthenticatedUserConfig.TEST_BUCKET, true);
    assertFalse(file.isPresent());
  }

}