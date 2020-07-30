package ca.gc.aafc.objectstore.api.rest;

import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.objectstore.api.DinaAuthenticatedUserConfig;
import ca.gc.aafc.objectstore.api.dto.ObjectSubtypeDto;
import ca.gc.aafc.objectstore.api.entities.DcType;
import io.crnk.core.engine.http.HttpStatus;

public class ObjectSubTypeJsonApiIT extends BaseJsonApiIntegrationTest {

  private ObjectSubtypeDto objectSubtype;
  private static final String SCHEMA_NAME = "ObjectSubtype";
  private static final String RESOURCE_UNDER_TEST = "object-subtype";
  private static final String SCHEMA_PATH = "DINA-Web/object-store-specs/master/schema/objectSubtype.yaml";  
  private static final String THUMB_TYPE_UUID = "34e4e0d8-91d8-4d52-99ae-ec42d6b0e66e";
  private final static String DINA_USER_NAME = DinaAuthenticatedUserConfig.USER_NAME;

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
    objectSubtype.setCreatedBy(DINA_USER_NAME);

    return toAttributeMap(objectSubtype);
  }

  @Override
  protected Map<String, Object> buildUpdateAttributeMap() {

    objectSubtype.setAcSubtype("MultimediaLearningObject".toUpperCase());
    objectSubtype.setDcType(DcType.MOVING_IMAGE);
    return toAttributeMap(objectSubtype);
  }

  @Test
  public void create_AsAppManaged_ReturnsUnAuthorized() {
    ObjectSubtypeDto dto = createRandomType();
    dto.setAppManaged(true);
    sendPost(getResourceUnderTest(), toJsonAPIMap(toAttributeMap(dto), null), HttpStatus.FORBIDDEN_403);
  }

  @Test
  public void delete_appManaged_ReturnsUnAuthorized() {
    sendDelete(THUMB_TYPE_UUID, HttpStatus.FORBIDDEN_403);
  }

  @Test
  public void update_ToAppManaged_ReturnsUnAuthorized() {
    ObjectSubtypeDto dto = createRandomType();
    dto.setAppManaged(false);
    String id = sendPost(toJsonAPIMap(toAttributeMap(dto), null));

    dto.setAppManaged(true);
    sendPatch(id, HttpStatus.FORBIDDEN_403, toJsonAPIMap(toAttributeMap(dto), null));
    sendDelete(id);
  }

  @Test
  public void update_FromAppManaged_ReturnsUnAuthorized() {
    ObjectSubtypeDto thumbnail = new ObjectSubtypeDto();
    thumbnail.setAppManaged(false);
    sendPatch(
      THUMB_TYPE_UUID,
      HttpStatus.FORBIDDEN_403,
      JsonAPITestHelper.toJsonAPIMap(
        getResourceUnderTest(),
        toAttributeMap(thumbnail),
        toRelationshipMap(buildRelationshipList()),
        THUMB_TYPE_UUID));
  }

  private static ObjectSubtypeDto createRandomType() {
    ObjectSubtypeDto dto = new ObjectSubtypeDto();
    dto.setDcType(DcType.SOUND);
    dto.setAcSubtype(RandomStringUtils.random(5));
    return dto;
  }

}
