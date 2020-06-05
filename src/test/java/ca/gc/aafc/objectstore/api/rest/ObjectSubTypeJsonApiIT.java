package ca.gc.aafc.objectstore.api.rest;

import java.util.Map;

import ca.gc.aafc.objectstore.api.dto.ObjectSubtypeDto;
import ca.gc.aafc.objectstore.api.entities.DcType;

public class ObjectSubTypeJsonApiIT extends BaseJsonApiIntegrationTest {

  private ObjectSubtypeDto objectSubtype;
  private static final String SCHEMA_NAME = "ObjectSubtype";
  private static final String RESOURCE_UNDER_TEST = "object-subtype";
  private static final String SCHEMA_PATH = "DINA-Web/object-store-specs/master/schema/objectSubtype.yaml";  
  
  @Override
  protected String getSchemaName() {
    return SCHEMA_NAME;
  }
  
  @Override
  protected String getSchemaPath() {
    return SCHEMA_PATH;
  }  
  
  @Override
  protected String getResourceUnderTest() {
    return RESOURCE_UNDER_TEST;
  }

  @Override
  protected Map<String, Object> buildCreateAttributeMap() {   
      
    objectSubtype = new ObjectSubtypeDto();
    objectSubtype.setUuid(null);
    objectSubtype.setDcType(DcType.SOUND);
    objectSubtype.setAcSubtype("MusicalNotation");

    return toAttributeMap(objectSubtype);
  }

  @Override
  protected Map<String, Object> buildUpdateAttributeMap() {

    objectSubtype.setAcSubtype("MultimediaLearningObject".toUpperCase());
    objectSubtype.setDcType(DcType.MOVING_IMAGE);
    return toAttributeMap(objectSubtype);
  }
 

}
