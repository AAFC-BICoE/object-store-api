package ca.gc.aafc.objectstore.api.rest;

import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.objectstore.api.DinaAuthenticatedUserConfig;
import ca.gc.aafc.objectstore.api.dto.ObjectSubtypeDto;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectSubtypeFactory;
import io.crnk.core.engine.http.HttpStatus;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class ObjectSubTypeJsonApiIT extends BaseJsonApiIntegrationTest {

  private ObjectSubtypeDto objectSubtype;
  private static final String SCHEMA_NAME = "ObjectSubtype";
  private static final String RESOURCE_UNDER_TEST = "object-subtype";
  private static final String DINA_USER_NAME = DinaAuthenticatedUserConfig.USER_NAME;
  private String appManagedId;

  @BeforeEach
  void setUp() {
    ObjectSubtype appManaged = ObjectSubtypeFactory
      .newObjectSubtype()
      .appManaged(true)
      .createdBy(DINA_USER_NAME)
      .build();

    // we need to run the setup in another transaction and commit it otherwise it can't be visible
    // to the test web server.
    service.runInNewTransaction(em -> em.persist(appManaged));

    appManagedId = appManaged.getUuid().toString();
  }

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
    objectSubtype.setCreatedBy(DINA_USER_NAME);
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
    sendGet(appManagedId).log().all(true);
    sendDelete(appManagedId, HttpStatus.FORBIDDEN_403);
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
      appManagedId,
      HttpStatus.FORBIDDEN_403,
      JsonAPITestHelper.toJsonAPIMap(
        getResourceUnderTest(),
        toAttributeMap(thumbnail),
        toRelationshipMap(buildRelationshipList()),
        appManagedId));
  }

  private static ObjectSubtypeDto createRandomType() {
    ObjectSubtypeDto dto = new ObjectSubtypeDto();
    dto.setDcType(DcType.SOUND);
    dto.setAcSubtype(RandomStringUtils.random(5));
    dto.setCreatedBy(DINA_USER_NAME);
    return dto;
  }

}
