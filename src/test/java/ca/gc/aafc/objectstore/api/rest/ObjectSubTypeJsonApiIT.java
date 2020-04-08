package ca.gc.aafc.objectstore.api.rest;

import java.util.Map;

import ca.gc.aafc.objectstore.api.dto.ObjectSubtypeDto;
import ca.gc.aafc.objectstore.api.entities.DcType;

public class ObjectSubTypeJsonApiIT extends BaseJsonApiIntegrationTest {

  private ObjectSubtypeDto objectSubtype;  
  
  @Override
  protected String getResourceUnderTest() {
    return "object-subtype";
  }

  @Override
  protected String getGetOneSchemaFilename() {
    return "getOneObjectSubtypeSchema.json";
  }

  @Override
  protected String getGetManySchemaFilename() {
    return null;
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
