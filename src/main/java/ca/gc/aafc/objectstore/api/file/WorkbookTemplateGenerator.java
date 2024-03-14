package ca.gc.aafc.objectstore.api.file;

import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import ca.gc.aafc.dina.workbook.WorkbookGenerator;
import ca.gc.aafc.objectstore.api.config.MediaTypeConfiguration;
import ca.gc.aafc.objectstore.api.minio.MinioFileService;
import ca.gc.aafc.objectstore.api.security.FileControllerAuthorizationService;
import ca.gc.aafc.objectstore.api.service.DerivativeService;
import ca.gc.aafc.objectstore.api.service.ObjectStoreMetaDataService;
import ca.gc.aafc.objectstore.api.service.ObjectUploadService;
import static ca.gc.aafc.objectstore.api.file.FileController.buildHttpHeaders;

import java.io.ByteArrayOutputStream;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class WorkbookTemplateGenerator {

  public static final String DIGEST_ALGORITHM = "SHA-1";
  private static final TikaConfig TIKA_CONFIG = TikaConfig.getDefaultConfig();

  @Inject
  public WorkbookTemplateGenerator(
      FileControllerAuthorizationService authorizationService,
      MinioFileService minioService,
      ObjectUploadService objectUploadService,
      DerivativeService derivativeService,
      ObjectStoreMetaDataService objectStoreMetaDataService,
      MediaTypeDetectionStrategy mediaTypeDetectionStrategy,
      DinaAuthenticatedUser authenticatedUser,
      MessageSource messageSource,
      MediaTypeConfiguration mediaTypeConfiguration) {
  }

  /**
   * Generates Workbook template from given columns
   *
   * @param columns
   * @return the Workbook template
   * @throws IOException
   */
  @PostMapping("workbook/generation")
  public ResponseEntity<ByteArrayResource> generateWorkbookTemplateFromColumns(
      @RequestAttribute("columns") List<String> columns)
      throws IOException {

    // size is quite small, load in memory to make it easier
    byte[] content;
    try (Workbook wb = WorkbookGenerator.generate(columns);
        ByteArrayOutputStream os = new ByteArrayOutputStream()) {
      wb.write(os);
      content = os.toByteArray();
    }

    return new ResponseEntity<>(new ByteArrayResource(content),
        buildHttpHeaders("template.xlsx", getMediaTypeForFilename("template.xlsx").toString(), content.length),
        HttpStatus.CREATED);
  }

   /**
   * Try to get the MediaType (Tika MediaType) from the filename with extension.
   * If not possible Octet Stream is returned.
   * @param filename
   * @return
   */
  public org.apache.tika.mime.MediaType getMediaTypeForFilename(String filename) {
    Metadata metadata = new Metadata();
    metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, filename);
    try {
      return TIKA_CONFIG.getDetector().detect(null, metadata);
    } catch (IOException e) {
      // ignore
    }
    return org.apache.tika.mime.MediaType.OCTET_STREAM;
  }

}
