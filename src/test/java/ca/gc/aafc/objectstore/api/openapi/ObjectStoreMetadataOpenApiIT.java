package ca.gc.aafc.objectstore.api.openapi;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.entities.Derivative.DerivativeType;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectSubtypeFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;
import lombok.SneakyThrows;

@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  classes = ObjectStoreApiLauncher.class
)
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
@Transactional
@ContextConfiguration(initializers = {PostgresTestContainerInitializer.class})
public class ObjectStoreMetadataOpenApiIT extends BaseRestAssuredTest {

  @Inject
  protected DatabaseSupportService service;

  private static final String SPEC_HOST = "raw.githubusercontent.com";
  private static final String ROOT_SPEC_PATH = "DINA-Web/object-store-specs/master/schema/object-store-api.yml";
  
  private static final String SCHEMA_NAME = "Metadata";
  private static final String RESOURCE_UNDER_TEST = "metadata";

  private static final URIBuilder URI_BUILDER = new URIBuilder();

  private ObjectSubtype oSubtype;
  private ObjectUpload oUpload;
  private ObjectUpload oUpload_derivative;
  private ObjectUpload oUpload_acDerivedFrom;
  private String derivativeUuid;
  
  static {
    URI_BUILDER.setScheme("https");
    URI_BUILDER.setHost(SPEC_HOST);
    URI_BUILDER.setPath(ROOT_SPEC_PATH);
  }

  protected ObjectStoreMetadataOpenApiIT() {
    super("/api/v1/");
  }

  public static URL getOpenAPISpecsURL() throws URISyntaxException, MalformedURLException {
    return URI_BUILDER.build().toURL();
  }
  
  @BeforeEach
  public void setup() {

    oUpload_derivative = ObjectUploadFactory.buildTestObjectUpload();
    oUpload_derivative.setIsDerivative(true);
    oUpload_derivative.setFileIdentifier(UUID.randomUUID());

    oUpload_acDerivedFrom = ObjectUploadFactory.buildTestObjectUpload();
    oUpload_acDerivedFrom.setFileIdentifier(UUID.randomUUID());

    oUpload = ObjectUploadFactory.buildTestObjectUpload();

    oSubtype = ObjectSubtypeFactory
      .newObjectSubtype()
      .build();
    
    // we need to run the setup in another transaction and commit it otherwise it can't be visible
    // to the test web server.
    service.runInNewTransaction(em -> {
      em.persist(oSubtype);
      em.persist(oUpload);
      em.persist(oUpload_derivative);
      em.persist(oUpload_acDerivedFrom);
    });

    ObjectStoreMetadataDto osMetadata = buildObjectStoreMetadataDto();
    osMetadata.setFileIdentifier(oUpload_acDerivedFrom.getFileIdentifier());
    String metadataUuid = sendPost("metadata", JsonAPITestHelper.toJsonAPIMap("metadata", JsonAPITestHelper.toAttributeMap(osMetadata), null, null)).extract().body().jsonPath().get("data.id");

    DerivativeDto derivative = buildDerivativeDto();
    derivative.setFileIdentifier(oUpload_derivative.getFileIdentifier());
    derivativeUuid = sendPost("derivative", JsonAPITestHelper.toJsonAPIMap("derivative", JsonAPITestHelper.toAttributeMap(derivative), 
    Map.of(
      "acDerivedFrom", getRelationshipType("metadata", metadataUuid)), null)).extract().body().jsonPath().get("data.id");

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
  }

  @Test
  @SneakyThrows
  void metadata_SpecValid() {
    ObjectStoreMetadataDto objectStoreMetadataDto = buildObjectStoreMetadataDto();
    OpenAPI3Assertions.assertRemoteSchema(getOpenAPISpecsURL(), SCHEMA_NAME,
    sendPost(RESOURCE_UNDER_TEST, JsonAPITestHelper.toJsonAPIMap(
      RESOURCE_UNDER_TEST, 
      JsonAPITestHelper.toAttributeMap(objectStoreMetadataDto),
      Map.of(
          "dcCreator", getRelationshipType("person", UUID.randomUUID().toString()),
          "acMetadataCreator", getRelationshipType("person", UUID.randomUUID().toString()),
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
    osMetadata.setDcType(oSubtype.getDcType());//on creation null should be accepted
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
    osMetadata.setAcSubType(oSubtype.getAcSubtype());
    osMetadata.setAcTags(new String[]{"acTags"});

    osMetadata.setDerivatives(null);
    return osMetadata;
  }

  private DerivativeDto buildDerivativeDto() {
    DerivativeDto dto = new DerivativeDto();
    dto.setDcType(DcType.IMAGE);
    dto.setAcDerivedFrom(null);
    dto.setGeneratedFromDerivative(null);
    dto.setDerivativeType(DerivativeType.THUMBNAIL_IMAGE);
    dto.setFileIdentifier(oUpload_derivative.getFileIdentifier());
    dto.setFileExtension(".jpg");
    dto.setAcHashFunction("abcFunction");
    dto.setAcHashValue("abc");
    dto.setDcFormat(MediaType.IMAGE_JPEG_VALUE);
    dto.setCreatedBy("user");
    return dto;
  }

  private Map<String, Object> getRelationshipListType(String type, String uuid) {
    return Map.of("data", List.of(Map.of(
      "id", uuid,
      "type", type)));
  }

  private Map<String, Object> getRelationshipType(String type, String uuid) {
    return Map.of("data", Map.of(
      "id", uuid,
      "type", type));
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
