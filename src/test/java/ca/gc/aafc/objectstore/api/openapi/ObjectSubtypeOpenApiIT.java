package ca.gc.aafc.objectstore.api.openapi;

import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.dina.testsupport.specs.OpenAPI3Assertions;
import ca.gc.aafc.dina.testsupport.specs.ValidationRestrictionOptions;
import ca.gc.aafc.objectstore.api.ObjectStoreApiLauncher;
import ca.gc.aafc.objectstore.api.dto.ObjectSubtypeDto;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.rest.ObjectStoreBaseRestAssuredTest;

import lombok.SneakyThrows;

@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  classes = ObjectStoreApiLauncher.class,
  properties = "dev-user.enabled=true"
)
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
@Transactional
@ContextConfiguration(initializers = {PostgresTestContainerInitializer.class})
public class ObjectSubtypeOpenApiIT extends ObjectStoreBaseRestAssuredTest {

  private static final String SCHEMA_NAME = "ObjectSubtype";
  private static final String RESOURCE_UNDER_TEST = "object-subtype";
  private static final String DINA_USER_NAME = "dev";

  protected ObjectSubtypeOpenApiIT() {
    super("/api/v1/");
  }

  @Test
  @SneakyThrows
  void  objectSubtype_SpecValid() {
    ObjectSubtypeDto objectSubtypeDto = buildObjectSubtypeDto();
    OpenAPI3Assertions.assertRemoteSchema(OpenAPIConstants.OBJECT_STORE_API_SPECS_URL, SCHEMA_NAME,
    sendPost(RESOURCE_UNDER_TEST, JsonAPITestHelper.toJsonAPIMap(
      RESOURCE_UNDER_TEST, 
      JsonAPITestHelper.toAttributeMap(objectSubtypeDto),
      null,
      null))
      .extract().asString(), ValidationRestrictionOptions.builder().allowAdditionalFields(true).build());
  }

  private ObjectSubtypeDto buildObjectSubtypeDto() {
    ObjectSubtypeDto objectSubtype = new ObjectSubtypeDto();
    objectSubtype.setUuid(null);
    objectSubtype.setDcType(DcType.SOUND);
    objectSubtype.setAcSubtype("MusicalNotation");
    objectSubtype.setCreatedBy(DINA_USER_NAME);

    return objectSubtype;
  }
  
}
