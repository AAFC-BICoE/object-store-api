package ca.gc.aafc.objectstore.api.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.context.WebApplicationContext;

import ca.gc.aafc.dina.testsupport.BaseRestAssuredTest;
import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPIRelationship;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.ObjectStoreApiLauncher;
import ca.gc.aafc.objectstore.api.dto.DerivativeDto;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.minio.MinioTestContainerInitializer;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;
import ca.gc.aafc.objectstore.api.testsupport.fixtures.DerivativeTestFixture;
import ca.gc.aafc.objectstore.api.testsupport.fixtures.ObjectStoreMetadataTestFixture;

import io.restassured.RestAssured;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.Header;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.MultiPartSpecification;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import javax.transaction.Transactional;

import static org.hamcrest.Matchers.hasItems;

@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  classes = ObjectStoreApiLauncher.class,
  properties = "dev-user.enabled=true"
)
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
@Transactional
@ContextConfiguration(initializers = {PostgresTestContainerInitializer.class, MinioTestContainerInitializer.class})
@Import(BaseIntegrationTest.ObjectStoreModuleTestConfiguration.class)
public class ObjectStoreMetadata2RestIT extends BaseRestAssuredTest {

  @Autowired
  protected WebApplicationContext wac;

  protected ObjectStoreMetadata2RestIT() {
    super("/api/v1/");
  }

  private static final String RESOURCE_UNDER_TEST = "metadata";

  private ObjectStoreMetadataDto buildObjectStoreMetadataDto() {
    OffsetDateTime dateTime4Test = OffsetDateTime.now();
    // file related data has to match what is set by TestConfiguration
    ObjectStoreMetadataDto osMetadata = ObjectStoreMetadataTestFixture.newObjectStoreMetadata();
    osMetadata.setUuid(null);
    osMetadata.setDcType(null); //on creation null should be accepted
    osMetadata.setXmpRightsWebStatement(null); // default value from configuration should be used
    osMetadata.setDcRights(null); // default value from configuration should be used
    osMetadata.setXmpRightsOwner(null); // default value from configuration should be used
    osMetadata.setXmpRightsUsageTerms(null); // default value from configuration should be used
    osMetadata.setAcDigitizationDate(dateTime4Test);
    osMetadata.setFileIdentifier(ObjectUploadFactory.TEST_FILE_IDENTIFIER);
    osMetadata.setFileExtension(ObjectUploadFactory.TEST_FILE_EXT);
    osMetadata.setBucket(ObjectUploadFactory.TEST_BUCKET);
    osMetadata.setPubliclyReleasable(true);
    osMetadata.setNotPubliclyReleasableReason("Classified");

    osMetadata.setDerivatives(null);
    return osMetadata;
  }

  private String uploadFile() {

    MultiPartSpecification file = new MultiPartSpecBuilder("Test Content".getBytes())
      .mimeType("text/plain")
      .fileName("testFile")
      .controlName("file")
      .build();

    return RestAssured.given()
      .header(new Header("content-type", "multipart/form-data"))
      .port(this.testPort)
      .multiPart(file)
      .when().post("/api/v1/file/" + ObjectUploadFactory.TEST_BUCKET).then()
      .statusCode(201)
      .extract()
      .body()
      .jsonPath()
      .get("data.id");
  }

  private String uploadDerivative() throws Exception {

    MultiPartSpecification file = new MultiPartSpecBuilder("Test Content".getBytes())
      .mimeType("text/plain")
      .fileName("testFile")
      .controlName("file")
      .build();

    return RestAssured.given()
      .header(new Header("content-type", "multipart/form-data"))
      .port(this.testPort)
      .multiPart(file)
      .when().post("/api/v1/file/" + ObjectUploadFactory.TEST_BUCKET + "/derivative/").then()
      .statusCode(201)
      .extract()
      .body()
      .jsonPath()
      .get("data.id");
  }

  @Test
  public void onFindAll_expectedResultReturned() throws Exception {

    UUID fileIdentifier = UUID.fromString(uploadFile());
    UUID fileIdentifierDerivative = UUID.fromString(uploadDerivative());

    ObjectStoreMetadataDto osMetadata = buildObjectStoreMetadataDto();
    osMetadata.setFileIdentifier(fileIdentifier);

    String metadataUUID = JsonAPITestHelper.extractId(sendPost(ObjectStoreMetadataDto.TYPENAME,
      JsonAPITestHelper.toJsonAPIMap(ObjectStoreMetadataDto.TYPENAME,
        JsonAPITestHelper.toAttributeMap(osMetadata),
null, null
      )));

    DerivativeDto derivativeDto = DerivativeTestFixture.newDerivative(fileIdentifierDerivative);
    sendPost(DerivativeDto.TYPENAME,
      JsonAPITestHelper.toJsonAPIMap(DerivativeDto.TYPENAME,
        JsonAPITestHelper.toAttributeMap(derivativeDto),
        JsonAPITestHelper.toRelationshipMap(
          JsonAPIRelationship.of("acDerivedFrom", ObjectStoreMetadataDto.TYPENAME, metadataUUID)), null));

    ValidatableResponse response = sendGet(RESOURCE_UNDER_TEST, "", Map.of("include", "derivatives,acMetadataCreator"), 200);
    response.body("data.id", hasItems(metadataUUID));

  }
}
