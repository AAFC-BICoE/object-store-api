package ca.gc.aafc.objectstore.api.rest;

import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.specification.RequestSpecification;
import java.io.IOException;
import java.util.UUID;
import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.ObjectStoreApiLauncher;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.dto.ResourceNameIdentifierResponseDto;
import ca.gc.aafc.objectstore.api.testsupport.fixtures.ObjectStoreMetadataTestFixture;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(
  classes = ObjectStoreApiLauncher.class,
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = "dev-user.enabled=true"
)
@TestPropertySource(properties = {"spring.config.additional-location=classpath:application-test.yml"})
@ContextConfiguration(initializers = PostgresTestContainerInitializer.class)
@Import(BaseIntegrationTest.ObjectStoreModuleTestConfiguration.class)
public class ResourceNameIdentifierRepositoryIT extends ObjectStoreBaseRestAssuredTest {

  @LocalServerPort
  protected int testPort;

  @Inject
  private ResourceLoader resourceLoader;

  private final static String TEST_BUCKET_NAME = "test";
  private final static String TEST_GROUP_NAME = TEST_BUCKET_NAME;

  protected ResourceNameIdentifierRepositoryIT() {
    super("/api/v1/");
  }

  @Test
  public void resourceNameIdentifierRepository_onGet_responseReturned() throws IOException {

    String fileIdentifier = newMultipart("drawing.png", MediaType.IMAGE_PNG_VALUE)
      .post("api/v1/file/" + TEST_BUCKET_NAME).then().statusCode(201)
      .extract().body().jsonPath().getString("data.id");

    ObjectStoreMetadataDto osMetadata = ObjectStoreMetadataTestFixture.newObjectStoreMetadata();
    osMetadata.setFileIdentifier(UUID.fromString(fileIdentifier));
    osMetadata.setBucket("test");
    osMetadata.setDerivatives(null);

    String createdUUID = sendPost(ObjectStoreMetadataDto.TYPENAME, JsonAPITestHelper.toJsonAPIMap(
      ObjectStoreMetadataDto.TYPENAME,
      JsonAPITestHelper.toAttributeMap(osMetadata),
      null,
      null)
    ).extract().body().jsonPath().getString("data.id");

    String uuid = newRequest().get("api/v1/" + ResourceNameIdentifierResponseDto.TYPE +
        "?filter[type][EQ]=metadata&filter[name][EQ]=" + "drawing.png" + "&filter[group][EQ]=" +
        TEST_GROUP_NAME)
      .then().extract().body().jsonPath().getString("data.id[0]");

    assertEquals(createdUUID, uuid);
  }

  private RequestSpecification newRequest() {
    return given()
      .config(RestAssured.config()
        .encoderConfig(EncoderConfig.encoderConfig()
          .defaultCharsetForContentType("UTF-8", JSON_API_CONTENT_TYPE)
          .defaultCharsetForContentType("UTF-8", JSON_PATCH_CONTENT_TYPE)))
      .port(testPort);
  }

  private RequestSpecification newMultipart(String filenameInClasspath, String mimeType) throws IOException {
    Resource testFile = resourceLoader.getResource("classpath:" + filenameInClasspath);
    return given()
      .config(RestAssured.config())
      .multiPart("file", testFile.getFile(), mimeType)
      .port(testPort);
  }

}
