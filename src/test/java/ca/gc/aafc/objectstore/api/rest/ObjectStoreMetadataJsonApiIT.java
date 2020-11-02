package ca.gc.aafc.objectstore.api.rest;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.gc.aafc.objectstore.api.MinioTestConfiguration;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectSubtypeFactory;
import io.restassured.response.ValidatableResponse;
import org.springframework.http.HttpStatus;

public class ObjectStoreMetadataJsonApiIT extends BaseJsonApiIntegrationTest {

  private static final String METADATA_DERIVED_PROPERTY_NAME = "acDerivedFrom";
  private static final String SCHEMA_NAME = "Metadata";
  private static final String RESOURCE_UNDER_TEST = "metadata";
  
  private ObjectStoreMetadataDto objectStoreMetadata;
  private ObjectSubtype oSubtype;
  private ObjectUpload oUpload;

  private UUID metadataId;
  
  @BeforeEach
  public void setup() {
    oUpload = MinioTestConfiguration.buildTestObjectUpload();

    // used to test relationships
    ObjectStoreMetadata metadata = ObjectStoreMetadataFactory
      .newObjectStoreMetadata()
      .fileIdentifier(UUID.randomUUID())
      .build();

    oSubtype = ObjectSubtypeFactory
      .newObjectSubtype()
      .build();
    
    // we need to run the setup in another transaction and commit it otherwise it can't be visible
    // to the test web server.
    service.runInNewTransaction(em -> {
      em.persist(metadata);
      em.persist(oSubtype);
      em.persist(oUpload);
    });

    metadataId = metadata.getUuid();
  }

  /**
   * Clean up database after each test.
   */
  @AfterEach
  public void tearDown() {
    deleteEntityByUUID("fileIdentifier", MinioTestConfiguration.TEST_THUMBNAIL_IDENTIFIER, ObjectStoreMetadata.class);
    deleteEntityByUUID("fileIdentifier", MinioTestConfiguration.TEST_FILE_IDENTIFIER, ObjectStoreMetadata.class);
    deleteEntityByUUID("uuid", oSubtype.getUuid(), ObjectSubtype.class);
    deleteEntityByUUID("fileIdentifier", MinioTestConfiguration.TEST_FILE_IDENTIFIER, ObjectUpload.class);
  }
  
  @Override
  protected String getResourceUnderTest() {
    return RESOURCE_UNDER_TEST;
  }

  @Override
  protected String getSchemaName() {
    return SCHEMA_NAME;
  }
  
  @Override
  protected Map<String, Object> buildCreateAttributeMap() {
    objectStoreMetadata = buildObjectStoreMetadataDto();
    return toAttributeMap(objectStoreMetadata);
  }

  private ObjectStoreMetadataDto buildObjectStoreMetadataDto() {
    OffsetDateTime dateTime4Test = OffsetDateTime.now();
    // file related data has to match what is set by TestConfiguration
    ObjectStoreMetadataDto osMetadata = new ObjectStoreMetadataDto();
    osMetadata.setUuid(null);
    osMetadata.setAcHashFunction("SHA-1");
    osMetadata.setDcType(null); //on creation null should be accepted
    osMetadata.setXmpRightsWebStatement(null); // default value from configuration should be used
    osMetadata.setDcRights(null); // default value from configuration should be used
    osMetadata.setXmpRightsOwner(null); // default value from configuration should be used
    osMetadata.setAcDigitizationDate(dateTime4Test);
    osMetadata.setFileIdentifier(MinioTestConfiguration.TEST_FILE_IDENTIFIER);
    osMetadata.setFileExtension(MinioTestConfiguration.TEST_FILE_EXT);
    osMetadata.setBucket(MinioTestConfiguration.TEST_BUCKET);
    osMetadata.setAcMetadataCreator(UUID.randomUUID());
    osMetadata.setDcCreator(UUID.randomUUID());
    osMetadata.setPubliclyReleasable(true);
    osMetadata.setNotPubliclyReleasableReason("Classified");
    osMetadata.setXmpRightsUsageTerms(null);
    return osMetadata;
  }

  @Test
  public void sendInvalidFileIdentifier() {
    ObjectStoreMetadataDto osMetadata = buildObjectStoreMetadataDto();
    osMetadata.setFileIdentifier(UUID.randomUUID());
    sendPost(getResourceUnderTest(), toJsonAPIMap(toAttributeMap(osMetadata), null),
        HttpStatus.UNPROCESSABLE_ENTITY.value());
  }

  @Override
  protected Map<String, Object> buildUpdateAttributeMap() {

    OffsetDateTime dateTime4TestUpdate = OffsetDateTime.now();
    objectStoreMetadata.setAcDigitizationDate(dateTime4TestUpdate);
    objectStoreMetadata.setDcType(oSubtype.getDcType());
    objectStoreMetadata.setAcSubType(oSubtype.getAcSubtype());
    return toAttributeMap(objectStoreMetadata);
  }
  
  @Override
  protected List<Relationship> buildRelationshipList() {
    return Arrays.asList(
      Relationship.of(METADATA_DERIVED_PROPERTY_NAME, "metadata", metadataId.toString()));
  }
  
  @Test
  public void resourceUnderTest_whenDeleteExisting_softDeletes() {
    String id = sendPost(toJsonAPIMap(buildCreateAttributeMap(), toRelationshipMap(buildRelationshipList())));

    sendDelete(id);

    // get list should not return deleted resource
    ValidatableResponse responseUpdate = sendGet("");
    responseUpdate.body("data.id", Matchers.not(Matchers.hasItem(Matchers.containsString(id))));

    // get list should return deleted resource with deleted filter
    responseUpdate = sendGet("?filter[softDeleted]");
    responseUpdate.body("data.id", Matchers.hasItem(Matchers.containsString(id)));

    // get one throws gone 410 as expected
    sendGet(id, 410);

    // get one resource is available with the deleted filter
    sendGet(id + "?filter[softDeleted]");
  }

}
