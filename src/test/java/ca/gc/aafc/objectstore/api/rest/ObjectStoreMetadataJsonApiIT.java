package ca.gc.aafc.objectstore.api.rest;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata.DcType;
import ca.gc.aafc.objectstore.api.mapper.ObjectStoreMetaMapperImpl;
import ca.gc.aafc.objectstore.api.mapper.ObjectStoreMetadataMapper;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;

public class ObjectStoreMetadataJsonApiIT extends BaseJsonApiIntegrationTest {

  private ObjectStoreMetadataMapper mapper = new ObjectStoreMetaMapperImpl();
  
  private ObjectStoreMetadata objectStoreMetadata;
  
  @Override
  protected String getResourceUnderTest() {
    return "object";
  }

  @Override
  protected String getGetOneSchemaFilename() {
    return "getOneMetadataSchema.json";
  }

  @Override
  protected String getGetManySchemaFilename() {
    return null;
  }

  @Override
  protected Map<String, Object> buildCreateAttributeMap() {
    
    OffsetDateTime dateTime4Test = OffsetDateTime.now();
    objectStoreMetadata = ObjectStoreMetadataFactory.newObjectStoreMetadata()
       .acHashFunction("MD5")
       .acDigitizationDate(dateTime4Test)
       .xmpMetadataDate(dateTime4Test)
      .build();
    ObjectStoreMetadataDto objectStoreMetadatadto = mapper.ObjectStoreMetadataToObjectStoreMetadataDto(objectStoreMetadata);
    Map<String, Object> map = toAttributeMap(objectStoreMetadatadto);
  
    
    return map;
  }

  @Override
  protected Map<String, Object> buildUpdateAttributeMap() {
    
    OffsetDateTime dateTime4TestUpdate = OffsetDateTime.now();
    objectStoreMetadata.setAcHashFunction("SHA1");
    objectStoreMetadata.setAcDigitizationDate(dateTime4TestUpdate);
    objectStoreMetadata.setXmpMetadataDate(dateTime4TestUpdate);    
    ObjectStoreMetadataDto objectStoreMetadatadto = mapper.ObjectStoreMetadataToObjectStoreMetadataDto(objectStoreMetadata);
    Map<String, Object> map = toAttributeMap(objectStoreMetadatadto);
   
    return map;    
  }
}
