package ca.gc.aafc.objectstore.api.file;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import ca.gc.aafc.dina.workbook.WorkbookConverter;
import ca.gc.aafc.dina.workbook.WorkbookSheet;
import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.testsupport.factories.MultipartFileFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Map;
import javax.inject.Inject;

@SpringBootTest(properties = "keycloak.enabled = true")
public class WorkbookGeneratorTest extends BaseIntegrationTest {

  @Inject
  private WorkbookController workbookTemplateController;

  @Inject
  private ResourceLoader resourceLoader;

  @Test
  public void fileUploadConversion_OnValidSpreadsheet_contentReturned() throws Exception {
    MockMultipartFile
      mockFile = MultipartFileFactory.createMockMultipartFile(resourceLoader,"test_spreadsheet.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE);
    Map<Integer, WorkbookSheet> workbook = workbookTemplateController.handleFileConversion(mockFile);
    assertFalse(workbook.isEmpty());
    assertFalse(workbook.get(0).rows().isEmpty());
  }

  @Test
  public void fileUploadConversion_OnValidCSV_contentReturned() throws Exception {
    // use Octet Stream to make sure the FileController will detect it's a csv
    MockMultipartFile mockFile = MultipartFileFactory.createMockMultipartFile(resourceLoader,"test_spreadsheet.csv", MediaType.APPLICATION_OCTET_STREAM_VALUE);
    Map<Integer, WorkbookSheet> workbook = workbookTemplateController.handleFileConversion(mockFile);
    assertFalse(workbook.isEmpty());
    assertFalse(workbook.get(0).rows().isEmpty());
  }

  @Test
  public void generateWorkbookTemplateFromColumns_OnValidColumns_contentReturned() throws Exception {
    ResponseEntity<ByteArrayResource> response = workbookTemplateController.generateWorkbookTemplateFromColumns(List.of("col 1", "col 2"));
    assertNotNull(response.getHeaders());
    assertEquals(response.getStatusCode(), HttpStatus.CREATED);
    assertNotNull(response.getBody());
    var result = WorkbookConverter.convertWorkbook(response.getBody().getInputStream());
    assertEquals("col 1", result.get(0).rows().getFirst().content()[0]);
  }
}
