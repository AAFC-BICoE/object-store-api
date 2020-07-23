package ca.gc.aafc.objectstore.api.rest;

import java.util.Map;

import org.junit.jupiter.api.Test;

import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.objectstore.api.dto.ObjectSubtypeDto;
import ca.gc.aafc.objectstore.api.entities.DcType;
import io.crnk.core.engine.http.HttpStatus;

public class ObjectSubTypeJsonApiIT extends BaseJsonApiIntegrationTest {

  private ObjectSubtypeDto objectSubtype;
  private static final String SCHEMA_NAME = "ObjectSubtype";
  private static final String RESOURCE_UNDER_TEST = "object-subtype";
  private static final String SCHEMA_PATH = "DINA-Web/object-store-specs/master/schema/objectSubtype.yaml";  
  private static final String THUMB_TYPE_UUID = "34e4e0d8-91d8-4d52-99ae-ec42d6b0e66e";

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
 
  @Test
  public void delete_appManaged_ReturnsUnAuthorized() {
    sendDelete(THUMB_TYPE_UUID, HttpStatus.FORBIDDEN_403);
  }

  @Test
  public void update_appManaged_ReturnsUnAuthorized() {
    ObjectSubtypeDto thumbnail = new ObjectSubtypeDto();
    thumbnail.setAppManaged(true);
    sendPatch(
      THUMB_TYPE_UUID,
      HttpStatus.FORBIDDEN_403,
      JsonAPITestHelper.toJsonAPIMap(
        getResourceUnderTest(),
        toAttributeMap(thumbnail),
        toRelationshipMap(buildRelationshipList()),
        THUMB_TYPE_UUID));
  }

}
