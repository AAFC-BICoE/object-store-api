package ca.gc.aafc.objectstore.api.openapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPIRelationship;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.dina.testsupport.specs.OpenAPI3Assertions;
import ca.gc.aafc.objectstore.api.ObjectStoreApiLauncher;
import ca.gc.aafc.objectstore.api.config.ObjectStoreVocabularyConfiguration;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreControlledVocabularyDto;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreControlledVocabularyItemDto;
import ca.gc.aafc.objectstore.api.rest.ObjectStoreBaseRestAssuredTest;
import ca.gc.aafc.objectstore.api.testsupport.fixtures.ObjectStoreControlledVocabularyItemTestFixture;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;

@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  classes = ObjectStoreApiLauncher.class,
  properties = "dev-user.enabled=true"
)
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
@Transactional
@ContextConfiguration(initializers = {PostgresTestContainerInitializer.class})
public class ObjectStoreControlledVocabularyOpenApiIT extends ObjectStoreBaseRestAssuredTest {

  public static final String TYPE_NAME = ObjectStoreControlledVocabularyItemDto.TYPENAME;

  protected ObjectStoreControlledVocabularyOpenApiIT() {
    super("/api/v1/");
  }

  @SneakyThrows
  @Test
  void CollectionControlledVocabulary_SpecValid() {

    ObjectStoreControlledVocabularyItemDto dto = ObjectStoreControlledVocabularyItemTestFixture.newObjectStoreControlledVocabularyItem();

    // validate against the colleciton-api specs since all controlled vocabulary is the same
    OpenAPI3Assertions
      .assertRemoteSchema(OpenAPIConstants.COLLECTION_API_SPECS_URL, "ControlledVocabularyItem",
        sendPost(
          ObjectStoreControlledVocabularyItemDto.TYPENAME,
          JsonAPITestHelper.toJsonAPIMap(
            ObjectStoreControlledVocabularyItemDto.TYPENAME,
            JsonAPITestHelper.toAttributeMap(dto),
            JsonAPITestHelper.toRelationshipMap(
              JsonAPIRelationship.of("controlledVocabulary",
                ObjectStoreControlledVocabularyDto.TYPENAME,
                ObjectStoreVocabularyConfiguration.MANAGED_ATTRIBUTE_VOCAB_UUID.toString())),
            null
          )
        ).extract()
          .asString());
  }
  
}
