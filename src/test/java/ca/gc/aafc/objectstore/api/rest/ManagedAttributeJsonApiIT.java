package ca.gc.aafc.objectstore.api.rest;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Map;

import ca.gc.aafc.objectstore.api.dto.ManagedAttributeDto;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute.ManagedAttributeType;
import ca.gc.aafc.objectstore.api.mapper.ManagedAttributeMapper;
import ca.gc.aafc.objectstore.api.testsupport.factories.ManagedAttributeFactory;
import groovyjarjarantlr.collections.List;

public class ManagedAttributeJsonApiIT extends BaseJsonApiIntegrationTest {

  private ManagedAttributeMapper mapper = ManagedAttributeMapper.INSTANCE;
  
  private ManagedAttribute managedAttribute;
  
  @Override
  protected String getResourceUnderTest() {
    return "managedAttribute";
  }

  @Override
  protected String getGetOneSchemaFilename() {
    return "getOneManagedAttributeSchema.json";
  }

  @Override
  protected String getGetManySchemaFilename() {
    return null;
  }

  @Override
  protected Map<String, Object> buildCreateAttributeMap() {
    String[] acceptedValues  = new String[] {"CataloguedObject"};
    
    managedAttribute = ManagedAttributeFactory.newManagedAttribute()
      .acceptedValues(acceptedValues)   
      .build();
    ManagedAttributeDto managedAttributeDto = mapper.toDto(managedAttribute);
    return toAttributeMap(managedAttributeDto);

  }

  @Override
  protected Map<String, Object> buildUpdateAttributeMap() {
    
    String[] acceptedValues  = new String[] {"dorsal"};
    
    managedAttribute.setName("specimen_view");
    managedAttribute.setAcceptedValues(acceptedValues);
    
    ManagedAttributeDto managedAttributeDto = mapper.toDto(managedAttribute);
    return toAttributeMap(managedAttributeDto);
    
  }
}
