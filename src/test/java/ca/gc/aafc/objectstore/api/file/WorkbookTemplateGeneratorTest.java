package ca.gc.aafc.objectstore.api.file;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ca.gc.aafc.dina.workbook.WorkbookConverter;
import ca.gc.aafc.objectstore.api.BaseIntegrationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import javax.inject.Inject;

@SpringBootTest(properties = "keycloak.enabled = true")
public class WorkbookTemplateGeneratorTest extends BaseIntegrationTest {

  @Inject
  private WorkbookController workbookTemplateController;

  @Test
  public void generateWorkbookTemplateFromColumns_OnValidColumns_contentReturned() throws Exception {
    ResponseEntity<ByteArrayResource> response = workbookTemplateController.generateWorkbookTemplateFromColumns(List.of("col 1", "col 2"));
    assertNotNull(response.getHeaders());
    assertEquals(response.getStatusCode(), HttpStatus.CREATED);
    assertNotNull(response.getBody());
    var result = WorkbookConverter.convertWorkbook(response.getBody().getInputStream());
    assertEquals("col 1", result.get(0).get(0).content()[0]);
  }

}
