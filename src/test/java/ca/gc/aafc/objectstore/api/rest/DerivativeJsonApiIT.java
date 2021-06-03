package ca.gc.aafc.objectstore.api.rest;

import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.objectstore.api.MinioTestConfiguration;
import ca.gc.aafc.objectstore.api.dto.DerivativeDto;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.entities.Derivative.DerivativeType;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectSubtypeFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DerivativeJsonApiIT extends BaseJsonApiIntegrationTest {

  private static final String SCHEMA_NAME = "Derivative";
  private static final String RESOURCE_UNDER_TEST = "derivative";
  
  private ObjectStoreMetadataDto objectStoreMetadata;
  private ObjectSubtype oSubtype;
  private ObjectUpload oUpload;
  private String metadataUuid;

  @BeforeEach
  public void setup() {
    oUpload = MinioTestConfiguration.buildTestObjectUpload();

    oSubtype = ObjectSubtypeFactory
      .newObjectSubtype()
      .build();
    
    // we need to run the setup in another transaction and commit it otherwise it can't be visible
    // to the test web server.
    service.runInNewTransaction(em -> {
      em.persist(oSubtype);
      em.persist(oUpload);
    });

  }

  /**
   * Clean up database after each test.
   */
  @AfterEach
  public void tearDown() {
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
    osMetadata.setXmpRightsUsageTerms(null); // default value from configuration should be used
    osMetadata.setAcDigitizationDate(dateTime4Test);
    osMetadata.setFileIdentifier(MinioTestConfiguration.TEST_FILE_IDENTIFIER);
    osMetadata.setFileExtension(MinioTestConfiguration.TEST_FILE_EXT);
    osMetadata.setBucket(MinioTestConfiguration.TEST_BUCKET);
    osMetadata.setPubliclyReleasable(true);
    osMetadata.setNotPubliclyReleasableReason("Classified");

    osMetadata.setDerivatives(null);
    return osMetadata;
  }

  @Test
  public void create_ReturnsCode201() {
    ObjectStoreMetadataDto osMetadata = buildObjectStoreMetadataDto();
    osMetadata.setFileIdentifier(oUpload.getFileIdentifier());
    metadataUuid = sendPost("metadata", JsonAPITestHelper.toJsonAPIMap("metadata", toAttributeMap(osMetadata), null, null),
        HttpStatus.CREATED.value());
    
    sendPost(getResourceUnderTest(), toJsonAPIMap(toAttributeMap(newDerivative(oUpload.getFileIdentifier())), toRelationshipMap(buildRelationshipList())), HttpStatus.CREATED.value());
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
      Relationship.of("acDerivedFrom", "metadata", metadataUuid));
  }

  private DerivativeDto newDerivative(UUID fileIdentifier) {
    DerivativeDto dto = new DerivativeDto();
    dto.setDcType(DcType.IMAGE);
    dto.setAcDerivedFrom(null);
    dto.setGeneratedFromDerivative(null);
    dto.setDerivativeType(DerivativeType.THUMBNAIL_IMAGE);
    dto.setFileIdentifier(fileIdentifier);
    dto.setFileExtension(".jpg");
    dto.setAcHashFunction("abcFunction");
    dto.setAcHashValue("abc");
    dto.setDcFormat(MediaType.IMAGE_JPEG_VALUE);
    dto.setCreatedBy("user");
    return dto;
  }

}
