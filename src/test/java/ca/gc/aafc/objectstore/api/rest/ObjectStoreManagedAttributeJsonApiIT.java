package ca.gc.aafc.objectstore.api.rest;

import java.util.HashMap;
import java.util.Map;

import ca.gc.aafc.dina.testsupport.factories.TestableEntityFactory;
import ca.gc.aafc.objectstore.api.DinaAuthenticatedUserConfig;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreManagedAttributeDto;
import ca.gc.aafc.dina.entity.ManagedAttribute.ManagedAttributeType;

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
    
    managedAttribute.setName("specimen_view");
    managedAttribute.setAcceptedValues(acceptedValues);
    
    return toAttributeMap(managedAttribute);
  }
}
