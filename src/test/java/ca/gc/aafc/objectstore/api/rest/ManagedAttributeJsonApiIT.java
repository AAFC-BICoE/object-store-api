package ca.gc.aafc.objectstore.api.rest;

import java.util.HashMap;
import java.util.Map;

import ca.gc.aafc.dina.testsupport.factories.TestableEntityFactory;
import ca.gc.aafc.objectstore.api.dto.ManagedAttributeDto;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute.ManagedAttributeType;

public class ManagedAttributeJsonApiIT extends BaseJsonApiIntegrationTest {

  private ManagedAttributeDto managedAttribute;

  @Override
  protected String getSchemaName() {
    return "ManagedAttribute";
  }
  
  @Override
  protected String getSchemaPath() {
    return "DINA-Web/object-store-specs/master/schema/managedAttribute.yaml";
  }
     
  @Override
  protected String getResourceUnderTest() {
    return "managed-attribute";
  }

  @Override
  protected Map<String, Object> buildCreateAttributeMap() {
    String[] acceptedValues  = new String[] {"CataloguedObject"};
    
    managedAttribute = new ManagedAttributeDto();
    managedAttribute.setAcceptedValues(acceptedValues);
    managedAttribute.setName(TestableEntityFactory.generateRandomNameLettersOnly(12));
    managedAttribute.setManagedAttributeType(ManagedAttributeType.STRING);
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
