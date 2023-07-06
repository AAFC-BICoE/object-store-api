package ca.gc.aafc.objectstore.api.service;

import org.apache.tika.mime.MimeTypeException;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.DinaAuthenticatedUserConfig;
import ca.gc.aafc.objectstore.api.dto.ObjectUploadDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.file.FileController;
import ca.gc.aafc.objectstore.api.minio.MinioTestContainerInitializer;
import ca.gc.aafc.objectstore.api.testsupport.factories.MultipartFileFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.inject.Inject;

@ContextConfiguration(initializers = MinioTestContainerInitializer.class)
public class MetadataServiceIT extends BaseIntegrationTest {

  private static final String TEST_UPLOAD_FILE_NAME = "drawing.png";

  @Inject
  private ResourceLoader resourceLoader;

  @Inject
  private FileController fileController;

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

  }

}
