package ca.gc.aafc.objectstore.api.file;

import ca.gc.aafc.dina.workbook.WorkbookConverter;
import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.DinaAuthenticatedUserConfig;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.minio.MinioTestContainerInitializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionTemplate;
import javax.inject.Inject;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(initializers = MinioTestContainerInitializer.class)
@SpringBootTest(properties = "keycloak.enabled = true")
public class WorkbookTemplateGeneratorTest extends BaseIntegrationTest {

  @Inject
  private WorkbookTemplateController workbookTemplateController;

  @Inject
  private TransactionTemplate transactionTemplate;

  private final static String bucketUnderTest = DinaAuthenticatedUserConfig.ROLES_PER_GROUPS.keySet().stream()
      .findFirst().get();

  @AfterEach
  public void cleanup() {
    // Delete the ObjectUploads that are not deleted automatically because they are
    // created
    // asynchronously outside the test's transaction:
    transactionTemplate.execute(
        transactionStatus -> {
          service.deleteByProperty(ObjectUpload.class, "bucket", bucketUnderTest);
          return null;
        });
  }

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
