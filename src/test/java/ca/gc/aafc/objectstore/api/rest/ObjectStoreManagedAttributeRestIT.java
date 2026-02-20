package ca.gc.aafc.objectstore.api.rest;

import javax.transaction.Transactional;

import ca.gc.aafc.dina.vocabulary.TypedVocabularyElement;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.dina.testsupport.factories.TestableEntityFactory;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.objectstore.api.ObjectStoreApiLauncher;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreManagedAttributeDto;
import ca.gc.aafc.objectstore.api.testsupport.factories.MultilingualDescriptionFactory;
import io.restassured.response.ValidatableResponse;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  classes = ObjectStoreApiLauncher.class,
  properties = "dev-user.enabled=true"
)
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
@Transactional
@ContextConfiguration(initializers = {PostgresTestContainerInitializer.class})
public class ObjectStoreManagedAttributeRestIT extends ObjectStoreBaseRestAssuredTest {

  protected ObjectStoreManagedAttributeRestIT() {
    super("/api/v1/");
  }

  private static final String RESOURCE_UNDER_TEST = "managed-attribute";

  private final static String DINA_USER_NAME = "dev";

  protected ObjectStoreManagedAttributeDto buildObjectStoreManagedAttributeDto() {
    String[] acceptedValues  = new String[] {"CataloguedObject"};
    
    ObjectStoreManagedAttributeDto managedAttribute = new ObjectStoreManagedAttributeDto();
    managedAttribute.setAcceptedValues(acceptedValues);
    managedAttribute.setName(TestableEntityFactory.generateRandomNameLettersOnly(12));
    managedAttribute.setVocabularyElementType(TypedVocabularyElement.VocabularyElementType.STRING);
    managedAttribute.setCreatedBy(DINA_USER_NAME);

    managedAttribute.setMultilingualDescription(MultilingualDescriptionFactory.newMultilingualDescription().build());
    
    return managedAttribute;
  }

  @Test
  public void resourceUnderTest_whenUpdatingImmutableFields_returnOkAndResourceIsNotUpdated() {
    // Setup: create an resource
    ObjectStoreManagedAttributeDto originalAttribute = buildObjectStoreManagedAttributeDto();

    String originalName = originalAttribute.getName();

    String id = sendPost(RESOURCE_UNDER_TEST, JsonAPITestHelper.toJsonAPIMap(
      RESOURCE_UNDER_TEST, 
      JsonAPITestHelper.toAttributeMap(originalAttribute)))
      .extract().body().jsonPath().get("data.id");
    ObjectStoreManagedAttributeDto updatedAttribute = originalAttribute;

    updatedAttribute.setName("updatedName");
    updatedAttribute.setKey("updatedKey");
    
    // update the resource
    sendPatch(RESOURCE_UNDER_TEST, id, JsonAPITestHelper.toJsonAPIMap(
      RESOURCE_UNDER_TEST, 
      JsonAPITestHelper.toAttributeMap(updatedAttribute), id));

    ValidatableResponse responseUpdate = sendGet(RESOURCE_UNDER_TEST, id);

    responseUpdate.body("data.attributes.key",
      not(updatedAttribute.getKey()));

    responseUpdate.body("data.attributes.name",
      equalTo(originalName));

    // cleanup
    sendDelete(RESOURCE_UNDER_TEST, id, HttpStatus.NO_CONTENT.value());
  }

}
