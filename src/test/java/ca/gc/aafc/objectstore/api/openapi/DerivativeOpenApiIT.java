package ca.gc.aafc.objectstore.api.openapi;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPIRelationship;
import ca.gc.aafc.dina.util.UUIDHelper;
import ca.gc.aafc.objectstore.api.testsupport.fixtures.DerivativeTestFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import ca.gc.aafc.dina.testsupport.BaseRestAssuredTest;
import ca.gc.aafc.dina.testsupport.DatabaseSupportService;
import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.dina.testsupport.specs.OpenAPI3Assertions;
import ca.gc.aafc.objectstore.api.ObjectStoreApiLauncher;
import ca.gc.aafc.objectstore.api.dto.DerivativeDto;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;
import lombok.SneakyThrows;

@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  classes = ObjectStoreApiLauncher.class
)
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
@Transactional
@ContextConfiguration(initializers = {PostgresTestContainerInitializer.class})
public class DerivativeOpenApiIT extends BaseRestAssuredTest {

  @Inject
  private DatabaseSupportService service;

  private static final String SCHEMA_NAME = "Derivative";
  private static final String RESOURCE_UNDER_TEST = "derivative";

  private ObjectUpload objectUpload;
  private ObjectUpload objectUpload_meta;
  private ObjectUpload objectUpload_generatedFrom;
  private String metadataUuid;
  private String derivativeUuid;
  
  protected DerivativeOpenApiIT() {
    super("/api/v1/");
  }

  @BeforeEach
  public void setup() {
    objectUpload = ObjectUploadFactory.buildTestObjectUpload();
    objectUpload.setFileIdentifier(UUIDHelper.generateUUIDv7());
    objectUpload.setIsDerivative(true);

    objectUpload_meta = ObjectUploadFactory.buildTestObjectUpload();

    objectUpload_generatedFrom = ObjectUploadFactory.buildTestObjectUpload();
    objectUpload_generatedFrom.setFileIdentifier(UUIDHelper.generateUUIDv7());
    objectUpload_generatedFrom.setIsDerivative(true);
    
    // we need to run the setup in another transaction and commit it otherwise it can't be visible
    // to the test web server.
    service.runInNewTransaction(em -> {
      em.persist(objectUpload);
      em.persist(objectUpload_meta);
      em.persist(objectUpload_generatedFrom);
    });

    ObjectStoreMetadataDto osMetadata = buildObjectStoreMetadataDto();

    metadataUuid = JsonAPITestHelper.extractId(sendPost("metadata",
            JsonAPITestHelper.toJsonAPIMap("metadata", JsonAPITestHelper.toAttributeMap(osMetadata), null, null)));

    DerivativeDto derivative = buildDerivativeDto(objectUpload_generatedFrom.getFileIdentifier());

    derivativeUuid = JsonAPITestHelper.extractId(sendPost(RESOURCE_UNDER_TEST, JsonAPITestHelper.toJsonAPIMap(
            DerivativeDto.TYPENAME,
                    JsonAPITestHelper.toAttributeMap(derivative),
                    JsonAPITestHelper.toRelationshipMap(
                                    JsonAPIRelationship.of("acDerivedFrom", "metadata", metadataUuid)
                    ),
      null)));
  }

  /**
   * Clean up database after each test.
   */
  @AfterEach
  public void tearDown() {
    deleteEntityByUUID("fileIdentifier", objectUpload.getFileIdentifier(), Derivative.class);
    deleteEntityByUUID("fileIdentifier", objectUpload_generatedFrom.getFileIdentifier(), Derivative.class);
    deleteEntityByUUID("fileIdentifier", objectUpload_meta.getFileIdentifier(), ObjectStoreMetadata.class);
    deleteEntityByUUID("fileIdentifier", objectUpload_meta.getFileIdentifier(), ObjectUpload.class);
    deleteEntityByUUID("fileIdentifier", objectUpload.getFileIdentifier(), ObjectUpload.class);
    deleteEntityByUUID("fileIdentifier", objectUpload_generatedFrom.getFileIdentifier(), ObjectUpload.class);
  }

  @Test
  @SneakyThrows
  void derivative_SpecValid() {
    DerivativeDto derivativeDto = buildDerivativeDto(objectUpload.getFileIdentifier());
    OpenAPI3Assertions.assertRemoteSchema(OpenAPIConstants.OBJECT_STORE_API_SPECS_URL, SCHEMA_NAME,

    sendPost(RESOURCE_UNDER_TEST, JsonAPITestHelper.toJsonAPIMap(
      RESOURCE_UNDER_TEST, 
      JsonAPITestHelper.toAttributeMap(derivativeDto),
            JsonAPITestHelper.toRelationshipMap(
                    List.of(
                            JsonAPIRelationship.of("acDerivedFrom", "metadata", metadataUuid),
                            JsonAPIRelationship.of("generatedFromDerivative", "derivative", derivativeUuid))),
      null)).extract().asString());
  }

  private DerivativeDto buildDerivativeDto(UUID fileIdentifier) {
    DerivativeDto dto = DerivativeTestFixture.newDerivative(fileIdentifier);
    dto.setAcDerivedFrom(null);
    dto.setGeneratedFromDerivative(null);
    return dto;
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
    osMetadata.setFileIdentifier(ObjectUploadFactory.TEST_FILE_IDENTIFIER);
    osMetadata.setFileExtension(ObjectUploadFactory.TEST_FILE_EXT);
    osMetadata.setBucket(ObjectUploadFactory.TEST_BUCKET);
    osMetadata.setPubliclyReleasable(true);
    osMetadata.setNotPubliclyReleasableReason("Classified");
    
    osMetadata.setDerivatives(null);
    return osMetadata;
  }

  private <T> void deleteEntityByUUID(String uuidPropertyName, UUID uuid, Class<T> entityClass) {
    service.runInNewTransaction(em -> {
      CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
      CriteriaDelete<T> query = criteriaBuilder.createCriteriaDelete(entityClass);
      Root<T> root = query.from(entityClass);
      query.where(criteriaBuilder.equal(root.get(uuidPropertyName), uuid));
      em.createQuery(query).executeUpdate();
    });
  }
  
}
