package ca.gc.aafc.objectstore.api.rest;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import ca.gc.aafc.dina.testsupport.factories.TestableEntityFactory;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.objectstore.api.DinaAuthenticatedUserConfig;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreManagedAttributeDto;
import io.restassured.response.ValidatableResponse;
import ca.gc.aafc.dina.entity.ManagedAttribute.ManagedAttributeType;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class ObjectStoreManagedAttributeJsonApiIT extends BaseJsonApiIntegrationTest {

  private ObjectStoreManagedAttributeDto managedAttribute;
  
  private static final String SCHEMA_NAME = "ManagedAttribute";
  private static final String RESOURCE_UNDER_TEST = "managed-attribute";

  private final static String DINA_USER_NAME = DinaAuthenticatedUserConfig.USER_NAME;

  @Override
  protected String getSchemaName() {
    return SCHEMA_NAME;
  }
     
  @Override
  protected String getResourceUnderTest() {
    return RESOURCE_UNDER_TEST;
  }

  @Override
  protected Map<String, Object> buildCreateAttributeMap() {
    String[] acceptedValues  = new String[] {"CataloguedObject"};
    
    managedAttribute = new ObjectStoreManagedAttributeDto();
    managedAttribute.setAcceptedValues(acceptedValues);
    managedAttribute.setName(TestableEntityFactory.generateRandomNameLettersOnly(12));
    managedAttribute.setManagedAttributeType(ManagedAttributeType.STRING);
    managedAttribute.setCreatedBy(DINA_USER_NAME);
    Map<String, String> desc = new HashMap<String, String>();
    desc.put("fr", "fr_desc");
    desc.put("en", "en_desc");
    managedAttribute.setDescription(desc);
    
    return toAttributeMap(managedAttribute);
  }

  @Override
  protected Map<String, Object> buildUpdateAttributeMap() {
    String[] acceptedValues  =  new String[] {"dorsal"};
    
    managedAttribute.setAcceptedValues(acceptedValues);
    
    return toAttributeMap(managedAttribute);
  }

  protected Map<String, Object> buildUpdateImmutableAttributeMap() {
    
    managedAttribute.setName("updatedName");
    managedAttribute.setKey("updatedKey");
    
    return toAttributeMap(managedAttribute);
  }

  @Test
  public void resourceUnderTest_whenUpdatingImmutableFields_returnOkAndResourceIsNotUpdated() {
    // Setup: create an resource

    Map<String, Object> attributeMap = buildCreateAttributeMap();

    String id = sendPost(toJsonAPIMap(attributeMap, toRelationshipMap(buildRelationshipList())));
    Map<String, Object> updatedAttributeMap = buildUpdateImmutableAttributeMap();
    
    // update the resource
    sendPatch(id, JsonAPITestHelper.toJsonAPIMap(getResourceUnderTest(), updatedAttributeMap, toRelationshipMap(buildRelationshipList()), id));

    ValidatableResponse responseUpdate = sendGet(id);

    responseUpdate.body("data.attributes.key",
      not(updatedAttributeMap.get("key")));

    responseUpdate.body("data.attributes.name",
      equalTo(attributeMap.get("name")));

    // cleanup
    sendDelete(id);
  }

  @Override
  protected void sendDelete(String id) {
    sendDelete(id, HttpStatus.METHOD_NOT_ALLOWED.value());
  }
}
