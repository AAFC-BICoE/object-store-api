package ca.gc.aafc.objectstore.api.rest;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata.DcType;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;

public class ObjectStoreMetadataJsonApiIT extends BaseJsonApiIntegrationTest {

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
    
    OffsetDateTime acDigitizationDate = OffsetDateTime.now();
    OffsetDateTime dateTime4 = OffsetDateTime.of(LocalDateTime.of(2019, 05, 12, 05, 45),
        ZoneOffset.ofHoursMinutes(6, 30));    
    ObjectStoreMetadata objectStoreMetadata = ObjectStoreMetadataFactory.newObjectStoreMetadata()
       .acHashFunction("MD5")
       .acDigitizationDate(acDigitizationDate)
      .build();
    
    Map<String, Object> map = toAttributeMap(objectStoreMetadata);
    
    return map;
  }

  @Override
  protected Map<String, Object> buildUpdateAttributeMap() {
    
    OffsetDateTime acDigitizationDate = OffsetDateTime.now();
    ObjectStoreMetadata objectStoreMetadata = ObjectStoreMetadataFactory.newObjectStoreMetadata()
        .acHashFunction("SHA1")
      .acDigitizationDate(acDigitizationDate)
      .build();
    
    Map<String, Object> map = toAttributeMap(objectStoreMetadata);
    
    return map;    
  }
}
