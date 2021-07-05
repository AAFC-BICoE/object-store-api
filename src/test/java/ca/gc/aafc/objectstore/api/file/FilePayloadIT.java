package ca.gc.aafc.objectstore.api.file;

import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.objectstore.api.DinaAuthenticatedUserConfig;
import ca.gc.aafc.objectstore.api.ObjectStoreApiLauncher;
import ca.gc.aafc.objectstore.api.minio.MinioTestContainerInitializer;
import io.restassured.RestAssured;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.Header;
import io.restassured.specification.MultiPartSpecification;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  classes = ObjectStoreApiLauncher.class)
@TestPropertySource(properties = {
  "spring.config.additional-location=classpath:application-test.yml",
  "spring.servlet.multipart.max-file-size=1KB"})
@Transactional
@ContextConfiguration(initializers = {PostgresTestContainerInitializer.class, MinioTestContainerInitializer.class})
public class FilePayloadIT {

  @LocalServerPort
  protected int testPort;

  private final static String bucketUnderTest = DinaAuthenticatedUserConfig.ROLES_PER_GROUPS.keySet()
    .stream()
    .findFirst()
    .get();

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
      .when().post("/api/v1/file/" + bucketUnderTest).then()
      .statusCode(413);
  }

}
