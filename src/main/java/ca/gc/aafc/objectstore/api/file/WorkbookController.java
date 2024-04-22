package ca.gc.aafc.objectstore.api.file;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeTypeException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import ca.gc.aafc.dina.workbook.DelimiterSeparatedConverter;
import ca.gc.aafc.dina.workbook.WorkbookConverter;
import ca.gc.aafc.dina.workbook.WorkbookGenerator;
import ca.gc.aafc.dina.workbook.WorkbookRow;

import static ca.gc.aafc.objectstore.api.file.FileController.buildHttpHeaders;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/workbook")
public class WorkbookController {

  private static final String TEMPLATE_FILENAME = "template.xlsx";
  private static final TikaConfig TIKA_CONFIG = TikaConfig.getDefaultConfig();

  private final MediaTypeDetectionStrategy mediaTypeDetectionStrategy;
  private final MessageSource messageSource;

  public WorkbookController(MediaTypeDetectionStrategy mediaTypeDetectionStrategy,
                            MessageSource messageSource) {
    this.mediaTypeDetectionStrategy = mediaTypeDetectionStrategy;
    this.messageSource = messageSource;
  }

  /**
   * Converts Workbooks (Excel files) or CSV/TSV (handled as Workbook with 1 sheet) to a
   * generic row-based JSON structure.
   *
   * @param file
   * @return the content of the workbook per sheets
   * @throws IOException
   * @throws MimeTypeException
   */
  @PostMapping("conversion")
  public Map<Integer, List<WorkbookRow>> handleFileConversion(
    @RequestParam("file") MultipartFile file
  ) throws IOException, MimeTypeException {
    MediaTypeDetectionStrategy.MediaTypeDetectionResult mtdr = mediaTypeDetectionStrategy
      .detectMediaType(file.getInputStream(), file.getContentType(), file.getOriginalFilename());
    MediaType detectedMediaType = mtdr.getDetectedMediaType();

    if (DelimiterSeparatedConverter.isSupported(detectedMediaType.toString())) {
      return Map.of(0,
        DelimiterSeparatedConverter.convert(file.getInputStream(), detectedMediaType.toString()));
    } else if (WorkbookConverter.isSupported(detectedMediaType.toString())) {
      return WorkbookConverter.convertWorkbook(file.getInputStream());
    }

    throw new UnsupportedMediaTypeStatusException(messageSource.getMessage(
      "upload.invalid_media_type", new String[]{detectedMediaType.toString()}, LocaleContextHolder.getLocale()));
  }

  /**
   * Generates Workbook template from given columns
   *
   * @param columns
   * @return the Workbook template
   * @throws IOException
   */
  @PostMapping("generation")
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
        buildHttpHeaders(TEMPLATE_FILENAME, getMediaTypeForFilename(TEMPLATE_FILENAME).toString(), content.length),
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
