package ca.gc.aafc.objectstore.api.rest;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.gc.aafc.objectstore.api.TestConfiguration;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.Agent;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.mapper.CycleAvoidingMappingContext;
import ca.gc.aafc.objectstore.api.mapper.ObjectStoreMetadataMapper;
import ca.gc.aafc.objectstore.api.testsupport.factories.AgentFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import io.restassured.response.ValidatableResponse;

public class ObjectStoreMetadataJsonApiIT extends BaseJsonApiIntegrationTest {

  private final ObjectStoreMetadataMapper mapper = ObjectStoreMetadataMapper.INSTANCE;
  private static final String METADATA_CREATOR_PROPERTY_NAME = "acMetadataCreator";
  private static final String DC_CREATOR_PROPERTY_NAME = "dcCreator";
  private ObjectStoreMetadata objectStoreMetadata;
  
  private final UUID agentId = UUID.randomUUID();

  @BeforeEach
  public void setup() {
    Agent agent = AgentFactory.newAgent()
        .uuid(agentId)
        .build();

    // we need to run the setup in another transaction and commit it otherwise it can't be visible
    // to the test web server.
    runInNewTransaction(em -> {
      em.persist(agent);
    });
  }

  /**
   * Clean up database after each test.
   */
  @AfterEach
  public void tearDown() {
    runInNewTransaction(em -> {
      CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
      CriteriaDelete<ObjectStoreMetadata> query = criteriaBuilder.createCriteriaDelete(ObjectStoreMetadata.class);
      Root<ObjectStoreMetadata> root = query.from(ObjectStoreMetadata.class);
      query.where(criteriaBuilder.equal(root.get("fileIdentifier"), TestConfiguration.TEST_FILE_IDENTIFIER));
      em.createQuery(query).executeUpdate();
    });
  }

  @Override
  protected String getResourceUnderTest() {
    return "metadata";
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
    // file related data has to match what is set by TestConfiguration
    objectStoreMetadata = ObjectStoreMetadataFactory.newObjectStoreMetadata()
       .uuid(null)
       .acHashFunction("SHA-1")
       .dcType(null) //on creation null should be accepted
       .xmpRightsWebStatement(null) // default value from configuration should be used
       .dcRights(null) // default value from configuration should be used
       .xmpRightsOwner(null) // default value from configuration should be used
       .acDigitizationDate(dateTime4Test)
       .fileIdentifier(TestConfiguration.TEST_FILE_IDENTIFIER)
       .fileExtension(TestConfiguration.TEST_FILE_EXT)
       .bucket(TestConfiguration.TEST_BUCKET)
       .acHashValue("123")
       .publiclyReleasable(true)
       .notPubliclyReleasableReason("Classified")
       .build();

    ObjectStoreMetadataDto objectStoreMetadatadto = mapper.toDto(objectStoreMetadata, null, new CycleAvoidingMappingContext());
    return toAttributeMap(objectStoreMetadatadto);
  }

  @Override
  protected Map<String, Object> buildUpdateAttributeMap() {

    OffsetDateTime dateTime4TestUpdate = OffsetDateTime.now();
    objectStoreMetadata.setAcDigitizationDate(dateTime4TestUpdate);
    ObjectStoreMetadataDto objectStoreMetadatadto = mapper.toDto(objectStoreMetadata, null, new CycleAvoidingMappingContext());
    return toAttributeMap(objectStoreMetadatadto);
  }
  
  @Override
  protected List<Relationship> buildRelationshipList() {
    List<Relationship> relationshipList = new LinkedList<Relationship>(Arrays.asList(Relationship.of(METADATA_CREATOR_PROPERTY_NAME, "agent", agentId.toString())));
    List<Relationship> dcCreatorRelationshipList = new LinkedList<Relationship>(Arrays.asList(Relationship.of(DC_CREATOR_PROPERTY_NAME, "agent", agentId.toString())));
    
    for (Relationship relation : dcCreatorRelationshipList) {
      relationshipList.add(relation);
    }
    
    return relationshipList;
  }
  
  @Test
  public void resourceUnderTest_whenDeleteExisting_softDeletes() {
    String id = sendPost(toJsonAPIMap(buildCreateAttributeMap(), toRelationshipMap(buildRelationshipList())));

    sendDelete(id);

    // get list should not return deleted resource
    ValidatableResponse responseUpdate = sendGet("");
    responseUpdate.body("data.id", Matchers.not(Matchers.hasItem(Matchers.containsString(id))));

    // get list should return deleted resource with deleted filter
    responseUpdate = sendGet("?filter[deletedDate][NEQ]=null");
    responseUpdate.body("data.id", Matchers.hasItem(Matchers.containsString(id)));

    // get one throws gone 410 as expected
    sendGet(id, 410);

    // get one resource is available with the deleted filter
    sendGet(id + "?filter[deletedDate][NEQ]=null");
  }

}
