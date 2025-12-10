package ca.gc.aafc.objectstore.api.openapi;

import ca.gc.aafc.dina.testsupport.DatabaseSupportService;
import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPIRelationship;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.dina.testsupport.specs.OpenAPI3Assertions;
import ca.gc.aafc.dina.util.UUIDHelper;
import ca.gc.aafc.objectstore.api.ObjectStoreApiLauncher;
import ca.gc.aafc.objectstore.api.dto.DerivativeDto;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.rest.ObjectStoreBaseRestAssuredTest;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreManagedAttributeFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectSubtypeFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;
import ca.gc.aafc.objectstore.api.testsupport.fixtures.DerivativeTestFixture;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  classes = ObjectStoreApiLauncher.class,
  properties = "dev-user.enabled=true")
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
@Transactional
@ContextConfiguration(initializers = { PostgresTestContainerInitializer.class })
public class ObjectStoreMetadataOpenApiIT extends ObjectStoreBaseRestAssuredTest {

  @Inject
  protected DatabaseSupportService service;

  private static final String SCHEMA_NAME = "Metadata";
  private static final String RESOURCE_UNDER_TEST = "metadata";

  private static final String MANAGED_ATTRIBUTE_KEY = "managed-attribute-key";
  private static final String MANAGED_ATTRIBUTE_VALUE = "option1";

  private ObjectSubtype oSubtype;
  private ObjectUpload oUpload;
  private ObjectUpload oUpload_derivative;
  private ObjectUpload oUpload_acDerivedFrom;
  private String derivativeUuid;
  private UUID managedAttributeUuid;

  protected ObjectStoreMetadataOpenApiIT() {
    super("/api/v1/");
  }

  @BeforeEach
  public void setup() {

    oUpload_derivative = ObjectUploadFactory.buildTestObjectUpload();
    oUpload_derivative.setIsDerivative(true);
    oUpload_derivative.setFileIdentifier(UUIDHelper.generateUUIDv7());

    oUpload_acDerivedFrom = ObjectUploadFactory.buildTestObjectUpload();
    oUpload_acDerivedFrom.setFileIdentifier(UUIDHelper.generateUUIDv7());

    oUpload = ObjectUploadFactory.buildTestObjectUpload();

    oSubtype = ObjectSubtypeFactory
        .newObjectSubtype()
        .build();

    // Add a managed attribute to use for the object store metadata testing.
    ObjectStoreManagedAttribute managedAttribute = ObjectStoreManagedAttributeFactory.newManagedAttribute()
        .uuid(UUIDHelper.generateUUIDv7())
        .name(MANAGED_ATTRIBUTE_KEY)
        .key(MANAGED_ATTRIBUTE_KEY)
        .acceptedValues(new String[] { MANAGED_ATTRIBUTE_VALUE })
        .createdBy("admin")
        .createdOn(OffsetDateTime.now())
        .build();
    managedAttributeUuid = managedAttribute.getUuid();

    // we need to run the setup in another transaction and commit it otherwise it
    // can't be visible
    // to the test web server.
    service.runInNewTransaction(em -> {
      em.persist(managedAttribute);
      em.persist(oSubtype);
      em.persist(oUpload);
      em.persist(oUpload_derivative);
      em.persist(oUpload_acDerivedFrom);
    });

    ObjectStoreMetadataDto osMetadata = buildObjectStoreMetadataDto();
    osMetadata.setFileIdentifier(oUpload_acDerivedFrom.getFileIdentifier());
    String metadataUuid = sendPost("metadata",
        JsonAPITestHelper.toJsonAPIMap("metadata", JsonAPITestHelper.toAttributeMap(osMetadata), null, null)).extract()
        .body().jsonPath().get("data.id");

    DerivativeDto derivative = buildDerivativeDto();
    derivative.setFileIdentifier(oUpload_derivative.getFileIdentifier());
    derivativeUuid = sendPost("derivative", JsonAPITestHelper.toJsonAPIMap(
        "derivative", JsonAPITestHelper.toAttributeMap(derivative),
        JsonAPITestHelper.toRelationshipMap(JsonAPIRelationship.of("acDerivedFrom", "metadata", metadataUuid)),
        null)).extract().body().jsonPath().get("data.id");
  }

  /**
   * Clean up database after each test.
   */
  @AfterEach
  public void tearDown() {
    deleteEntityByUUID("fileIdentifier", oUpload_derivative.getFileIdentifier(), Derivative.class);
    deleteEntityByUUID("fileIdentifier", ObjectUploadFactory.TEST_FILE_IDENTIFIER, ObjectStoreMetadata.class);
    deleteEntityByUUID("fileIdentifier", oUpload_acDerivedFrom.getFileIdentifier(), ObjectStoreMetadata.class);
    deleteEntityByUUID("uuid", oSubtype.getUuid(), ObjectSubtype.class);
    deleteEntityByUUID("fileIdentifier", oUpload_derivative.getFileIdentifier(), ObjectUpload.class);
    deleteEntityByUUID("fileIdentifier", ObjectUploadFactory.TEST_FILE_IDENTIFIER, ObjectUpload.class);
    deleteEntityByUUID("fileIdentifier", oUpload_acDerivedFrom.getFileIdentifier(), ObjectUpload.class);
    deleteEntityByUUID("uuid", managedAttributeUuid, ObjectStoreManagedAttribute.class);
  }

  @Test
  @SneakyThrows
  void metadata_SpecValid() {
    ObjectStoreMetadataDto objectStoreMetadataDto = buildObjectStoreMetadataDto();
    OpenAPI3Assertions.assertRemoteSchema(OpenAPIConstants.OBJECT_STORE_API_SPECS_URL, SCHEMA_NAME,
        sendPost(RESOURCE_UNDER_TEST, JsonAPITestHelper.toJsonAPIMap(
            RESOURCE_UNDER_TEST,
            JsonAPITestHelper.toAttributeMap(objectStoreMetadataDto),
            Map.of(
                "dcCreator", JsonAPITestHelper.generateExternalRelation("person"),
                "acMetadataCreator", JsonAPITestHelper.generateExternalRelation("person"),
                "derivatives", getRelationshipListType("derivative", derivativeUuid)),
            null))
            .extract().asString());
  }

  private ObjectStoreMetadataDto buildObjectStoreMetadataDto() {
    OffsetDateTime dateTime4Test = OffsetDateTime.now();

    // file related data has to match what is set by TestConfiguration
    ObjectStoreMetadataDto osMetadata = new ObjectStoreMetadataDto();
    osMetadata.setUuid(null);
    osMetadata.setAcHashFunction("SHA-1");
    osMetadata.setDcType(oSubtype.getDcType());// on creation null should be accepted
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
    osMetadata.setDcCreator(null);
    osMetadata.setAcMetadataCreator(null);
    osMetadata.setAcCaption("acCaption");
    osMetadata.setAcSubtype(oSubtype.getAcSubtype());
    osMetadata.setAcTags(new String[] { "acTags" });
    osMetadata.setManagedAttributes(Map.of(MANAGED_ATTRIBUTE_KEY, MANAGED_ATTRIBUTE_VALUE));

    osMetadata.setDerivatives(null);
    return osMetadata;
  }

  private DerivativeDto buildDerivativeDto() {
    DerivativeDto dto = DerivativeTestFixture.newDerivative(oUpload_derivative.getFileIdentifier());
    dto.setAcDerivedFrom(null);
    dto.setGeneratedFromDerivative(null);
    return dto;
  }

  private Map<String, Object> getRelationshipListType(String type, String uuid) {
    return Map.of("data", List.of(Map.of(
        "id", uuid,
        "type", type)));
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
