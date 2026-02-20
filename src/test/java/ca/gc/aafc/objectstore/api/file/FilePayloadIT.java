package ca.gc.aafc.objectstore.api.file;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.ObjectStoreApiLauncher;
import io.restassured.RestAssured;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.Header;
import io.restassured.specification.MultiPartSpecification;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import java.security.SecureRandom;

@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  classes = ObjectStoreApiLauncher.class)
@TestPropertySource(properties = {
  "spring.config.additional-location=classpath:application-test.yml",
  "spring.servlet.multipart.max-file-size=1KB",
  "dev-user.enabled=true"})
public class FilePayloadIT extends BaseIntegrationTest {

  @LocalServerPort
  protected int testPort;

  private final static String TEST_BUCKET_NAME = "test";

  @Test
  public void fileUpload_WhenPayloadToLarge_ReturnsPayLoadToLarge() {
    byte[] bytes = new byte[20048];
    new SecureRandom().nextBytes(bytes);

    MultiPartSpecification file = new MultiPartSpecBuilder(bytes)
      .mimeType("text/plain")
      .fileName("testFile")
      .controlName("file")
      .build();

    RestAssured.given()
      .header(new Header("content-type", "multipart/form-data"))
      .port(this.testPort)
      .multiPart(file)
      .when().post("/api/v1/file/" + TEST_BUCKET_NAME).then()
      .statusCode(413);
  }

}
