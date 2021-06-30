package ca.gc.aafc.objectstore.api.openapi;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import ca.gc.aafc.dina.testsupport.BaseRestAssuredTest;
import ca.gc.aafc.dina.testsupport.DatabaseSupportService;
import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.dina.testsupport.specs.OpenAPI3Assertions;
import ca.gc.aafc.dina.testsupport.specs.ValidationRestrictionOptions;
import ca.gc.aafc.objectstore.api.ObjectStoreApiLauncher;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
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
    oUpload = ObjectUploadFactory.buildTestObjectUpload();

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
    deleteEntityByUUID("fileIdentifier", ObjectUploadFactory.TEST_FILE_IDENTIFIER, ObjectStoreMetadata.class);
    deleteEntityByUUID("uuid", oSubtype.getUuid(), ObjectSubtype.class);
    deleteEntityByUUID("fileIdentifier", ObjectUploadFactory.TEST_FILE_IDENTIFIER, ObjectUpload.class);
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
          "dcCreator", getExternalType("person"),
          "acMetadataCreator", getExternalType("person")),
      null))
      .extract().asString(), ValidationRestrictionOptions.builder().allowAdditionalFields(true).allowableMissingFields(Set.of("acDerivedFrom", "deletedDate", "managedAttributes", "acSubtype")).build());

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
    osMetadata.setDcCreator(null);
    osMetadata.setAcMetadataCreator(null);
    osMetadata.setAcCaption("acCaption");
    osMetadata.setAcSubType("acSubType");
    osMetadata.setAcTags(new String[]{"acTags"});

    osMetadata.setDerivatives(null);
    return osMetadata;
  }

  private Map<String, Object> getExternalType(String type) {
    return Map.of("data", Map.of(
      "id", UUID.randomUUID().toString(),
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
