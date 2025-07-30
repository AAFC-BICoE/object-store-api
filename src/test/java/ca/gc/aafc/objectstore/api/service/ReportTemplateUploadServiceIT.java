package ca.gc.aafc.objectstore.api.service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import javax.inject.Inject;

import org.apache.tika.mime.MimeTypeException;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;

import ca.gc.aafc.dina.repository.JsonApiModelAssistant;
import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.config.MediaTypeConfiguration;
import ca.gc.aafc.objectstore.api.file.FileController;
import ca.gc.aafc.objectstore.api.file.TemporaryObjectAccessController;
import ca.gc.aafc.objectstore.api.minio.MinioTestContainerInitializer;
import ca.gc.aafc.objectstore.api.testsupport.factories.MultipartFileFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ContextConfiguration(initializers = MinioTestContainerInitializer.class)
public class ReportTemplateUploadServiceIT extends BaseIntegrationTest {

  private static final String TEST_UPLOAD_FILE_NAME = "demo.ftlh";
  private final static String TEST_BUCKET_NAME = "test";

  @Inject
  private ResourceLoader resourceLoader;

  @Inject
  private FileController fileController;

  @Inject
  private ReportTemplateUploadService reportTemplateUploadService;

  @Inject
  private TemporaryObjectAccessController toaController;

  @Test
  public void reportTemplateUploadService_onTemplateUpload_toaDownloadAvailable() throws IOException, MimeTypeException, NoSuchAlgorithmException {
    // 1 - Upload report template
    MockMultipartFile mockFile = MultipartFileFactory
      .createMockMultipartFile(resourceLoader, TEST_UPLOAD_FILE_NAME,
        MediaTypeConfiguration.FREEMARKER_TEMPLATE_MIME_TYPE.toString());

    var uploadResponse = fileController.handleFileUpload(mockFile, TEST_BUCKET_NAME);
    UUID objectUploadUuid = JsonApiModelAssistant.extractUUIDFromRepresentationModelLink(uploadResponse);
    assertNotNull(objectUploadUuid);

    // 2 - Tell the ReportTemplateUploadService that the ObjectUpload is a report template
    ReportTemplateUploadService.ReportTemplateUploadResult result =
      reportTemplateUploadService.handleTemplateUpload(objectUploadUuid);

    // 3- make sure we can get the export file using the toa key
    assertEquals(200, toaController.downloadObject(result.toaKey()).getStatusCode().value());

  }
}
