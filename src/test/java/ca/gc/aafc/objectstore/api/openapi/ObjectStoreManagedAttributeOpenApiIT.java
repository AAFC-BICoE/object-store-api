package ca.gc.aafc.objectstore.api.openapi;

import javax.transaction.Transactional;

import ca.gc.aafc.dina.vocabulary.TypedVocabularyElement;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.dina.testsupport.factories.TestableEntityFactory;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.dina.testsupport.specs.OpenAPI3Assertions;
import ca.gc.aafc.objectstore.api.ObjectStoreApiLauncher;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreManagedAttributeDto;
import ca.gc.aafc.objectstore.api.rest.ObjectStoreBaseRestAssuredTest;
import ca.gc.aafc.objectstore.api.testsupport.factories.MultilingualDescriptionFactory;
import lombok.SneakyThrows;

@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  classes = ObjectStoreApiLauncher.class,
  properties = "dev-user.enabled=true"
)
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
@Transactional
@ContextConfiguration(initializers = {PostgresTestContainerInitializer.class})
public class ObjectStoreManagedAttributeOpenApiIT extends ObjectStoreBaseRestAssuredTest {

  private static final String SCHEMA_NAME = "ManagedAttribute";
  private static final String RESOURCE_UNDER_TEST = "managed-attribute";

  private final static String DINA_USER_NAME = "dev";

  protected ObjectStoreManagedAttributeOpenApiIT() {
    super("/api/v1/");
  }

  @Test
  @SneakyThrows
  void managedAttribute_SpecValid() {
    ObjectStoreManagedAttributeDto objectStoreManagedAttributeDto = buildObjectStoreManagedAttributeDto();
    OpenAPI3Assertions.assertRemoteSchema(OpenAPIConstants.OBJECT_STORE_API_SPECS_URL, SCHEMA_NAME,
    sendPost(RESOURCE_UNDER_TEST, JsonAPITestHelper.toJsonAPIMap(
      RESOURCE_UNDER_TEST, 
      JsonAPITestHelper.toAttributeMap(objectStoreManagedAttributeDto),
      null,
      null))
      .extract().asString());
  }

  private ObjectStoreManagedAttributeDto buildObjectStoreManagedAttributeDto() {
    String[] acceptedValues  = new String[] {"CataloguedObject"};

    ObjectStoreManagedAttributeDto managedAttribute = new ObjectStoreManagedAttributeDto();
    managedAttribute.setAcceptedValues(acceptedValues);
    managedAttribute.setName(TestableEntityFactory.generateRandomNameLettersOnly(12));
    managedAttribute.setVocabularyElementType(TypedVocabularyElement.VocabularyElementType.STRING);
    managedAttribute.setCreatedBy(DINA_USER_NAME);

    managedAttribute.setMultilingualDescription(MultilingualDescriptionFactory.newMultilingualDescription().build());

    return managedAttribute;
  }
  
}
