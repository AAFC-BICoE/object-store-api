package ca.gc.aafc.objectstore.api.rest;

import static io.restassured.RestAssured.given;

import java.util.HashMap;
import java.util.Map;

import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import ca.gc.aafc.dina.testsupport.BaseRestAssuredTest;
import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.dina.testsupport.factories.TestableEntityFactory;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.objectstore.api.DinaAuthenticatedUserConfig;
import ca.gc.aafc.objectstore.api.ObjectStoreApiLauncher;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreManagedAttributeDto;
import io.restassured.response.ValidatableResponse;
import ca.gc.aafc.dina.entity.ManagedAttribute.ManagedAttributeType;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  classes = ObjectStoreApiLauncher.class
)
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
@Transactional
@ContextConfiguration(initializers = {PostgresTestContainerInitializer.class})
public class ObjectStoreManagedAttributeRestIT extends BaseRestAssuredTest {

  protected ObjectStoreManagedAttributeRestIT() {
    super("/api/v1/");
  }

  private static final String RESOURCE_UNDER_TEST = "managed-attribute";

  private final static String DINA_USER_NAME = DinaAuthenticatedUserConfig.USER_NAME;

  protected ObjectStoreManagedAttributeDto buildObjectStoreManagedAttributeDto() {
    String[] acceptedValues  = new String[] {"CataloguedObject"};
    
    ObjectStoreManagedAttributeDto managedAttribute = new ObjectStoreManagedAttributeDto();
    managedAttribute.setAcceptedValues(acceptedValues);
    managedAttribute.setName(TestableEntityFactory.generateRandomNameLettersOnly(12));
    managedAttribute.setManagedAttributeType(ManagedAttributeType.STRING);
    managedAttribute.setCreatedBy(DINA_USER_NAME);
    Map<String, String> desc = new HashMap<String, String>();
    desc.put("fr", "fr_desc");
    desc.put("en", "en_desc");
    managedAttribute.setDescription(desc);
    
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
      JsonAPITestHelper.toAttributeMap(updatedAttribute)));

    ValidatableResponse responseUpdate = sendGet(RESOURCE_UNDER_TEST, id);

    responseUpdate.body("data.attributes.key",
      not(updatedAttribute.getKey()));

    responseUpdate.body("data.attributes.name",
      equalTo(originalName));

    // cleanup
    sendDelete(RESOURCE_UNDER_TEST, id, HttpStatus.METHOD_NOT_ALLOWED.value());
  }

}