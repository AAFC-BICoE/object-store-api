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

import ca.gc.aafc.objectstore.api.TestConfiguration;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectSubtypeFactory;
import io.restassured.response.ValidatableResponse;

public class ObjectStoreMetadataJsonApiIT extends BaseJsonApiIntegrationTest {

  private static final String METADATA_DERIVED_PROPERTY_NAME = "acDerivedFrom";
  
  private ObjectStoreMetadataDto objectStoreMetadata;
  private ObjectSubtype oSubtype;

  private UUID metadataId;

  @BeforeEach
  public void setup() {
    ObjectStoreMetadata metadata = ObjectStoreMetadataFactory
      .newObjectStoreMetadata()
      .uuid(metadataId)
      .fileIdentifier(UUID.randomUUID())
      .build();

    oSubtype = ObjectSubtypeFactory
      .newObjectSubtype()
      .build();

    // we need to run the setup in another transaction and commit it otherwise it can't be visible
    // to the test web server.
    runInNewTransaction(em -> {
      em.persist(metadata);
      em.persist(oSubtype);
    });

    metadataId = metadata.getUuid();
  }

  /**
   * Clean up database after each test.
   */
  @AfterEach
  public void tearDown() {
    deleteEntityByUUID("fileIdentifier", TestConfiguration.TEST_THUMBNAIL_IDENTIFIER, ObjectStoreMetadata.class);
    deleteEntityByUUID("fileIdentifier", TestConfiguration.TEST_FILE_IDENTIFIER, ObjectStoreMetadata.class);
    deleteEntityByUUID("uuid", oSubtype.getUuid(), ObjectSubtype.class);
  }
  
  @Override
  protected String getResourceUnderTest() {
    return "metadata";
  }

  @Override
  protected String getSchemaName() {
    return "Metadata";
  }
  
  @Override
  protected String getSchemaPath() {
    return "DINA-Web/object-store-specs/master/schema/metadata.yaml";
  }
  
  @Override
  protected Map<String, Object> buildCreateAttributeMap() {
    
    OffsetDateTime dateTime4Test = OffsetDateTime.now();
    // file related data has to match what is set by TestConfiguration
    objectStoreMetadata = new ObjectStoreMetadataDto();
    objectStoreMetadata.setUuid(null);
    objectStoreMetadata.setAcHashFunction("SHA-1");
    objectStoreMetadata.setDcType(null); //on creation null should be accepted
    objectStoreMetadata.setXmpRightsWebStatement(null); // default value from configuration should be used
    objectStoreMetadata.setDcRights(null); // default value from configuration should be used
    objectStoreMetadata.setXmpRightsOwner(null); // default value from configuration should be used
    objectStoreMetadata.setAcDigitizationDate(dateTime4Test);
    objectStoreMetadata.setFileIdentifier(TestConfiguration.TEST_FILE_IDENTIFIER);
    objectStoreMetadata.setFileExtension(TestConfiguration.TEST_FILE_EXT);
    objectStoreMetadata.setBucket(TestConfiguration.TEST_BUCKET);
    objectStoreMetadata.setAcHashValue("123");
    objectStoreMetadata.setAcMetadataCreator(UUID.randomUUID());
    objectStoreMetadata.setDcCreator(UUID.randomUUID());
    objectStoreMetadata.setPubliclyReleasable(true);
    objectStoreMetadata.setNotPubliclyReleasableReason("Classified");
    objectStoreMetadata.setXmpRightsUsageTerms(null);

    return toAttributeMap(objectStoreMetadata);
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
