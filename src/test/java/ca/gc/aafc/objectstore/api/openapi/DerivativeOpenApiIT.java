package ca.gc.aafc.objectstore.api.openapi;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.Map;

import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import org.apache.http.client.utils.URIBuilder;
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
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.entities.Derivative.DerivativeType;
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
    objectUpload.setFileIdentifier(UUID.randomUUID());
    objectUpload.setIsDerivative(true);

    objectUpload_meta = ObjectUploadFactory.buildTestObjectUpload();

    objectUpload_generatedFrom = ObjectUploadFactory.buildTestObjectUpload();
    objectUpload_generatedFrom.setFileIdentifier(UUID.randomUUID());
    objectUpload_generatedFrom.setIsDerivative(true);
    
    // we need to run the setup in another transaction and commit it otherwise it can't be visible
    // to the test web server.
    service.runInNewTransaction(em -> {
      em.persist(objectUpload);
      em.persist(objectUpload_meta);
      em.persist(objectUpload_generatedFrom);
    });

    ObjectStoreMetadataDto osMetadata = buildObjectStoreMetadataDto();

    metadataUuid = sendPost("metadata", JsonAPITestHelper.toJsonAPIMap("metadata", JsonAPITestHelper.toAttributeMap(osMetadata), null, null)).extract().body().jsonPath().get("data.id");

    DerivativeDto derivative = buildDerivativeDto(objectUpload_generatedFrom.getFileIdentifier());

    derivativeUuid = sendPost(RESOURCE_UNDER_TEST, JsonAPITestHelper.toJsonAPIMap(
      RESOURCE_UNDER_TEST, 
      JsonAPITestHelper.toAttributeMap(derivative),
      Map.of(
          "acDerivedFrom", getRelationshipType("metadata", metadataUuid)),
      null)).extract().body().jsonPath().get("data.id");

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
      Map.of(
          "acDerivedFrom", getRelationshipType("metadata", metadataUuid),
          "generatedFromDerivative", getRelationshipType("derivative", derivativeUuid)),
      null))
      .extract().asString());
  }

  private Map<String, Object> getRelationshipType(String type, String uuid) {
    return Map.of("data", Map.of(
      "id", uuid,
      "type", type));
  }

  private DerivativeDto buildDerivativeDto(UUID fileIdentifier) {
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
