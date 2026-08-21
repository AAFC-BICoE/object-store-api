package ca.gc.aafc.objectstore.api.rest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPIRelationship;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.objectstore.api.ObjectStoreApiLauncher;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreControlledVocabularyDto;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreControlledVocabularyItemDto;
import ca.gc.aafc.objectstore.api.testsupport.fixtures.ObjectStoreControlledVocabularyItemTestFixture;
import ca.gc.aafc.objectstore.api.testsupport.fixtures.ObjectStoreControlledVocabularyTestFixture;
import io.restassured.response.ValidatableResponse;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import org.springframework.http.HttpStatus;

@SpringBootTest(
  classes = ObjectStoreApiLauncher.class,
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = "dev-user.enabled=true"
)
@TestPropertySource(properties = {"spring.config.additional-location=classpath:application-test.yml"})
@ContextConfiguration(initializers = {PostgresTestContainerInitializer.class})
public class ObjectStoreControlledVocabularyItemRestIT extends ObjectStoreBaseRestAssuredTest {

  protected ObjectStoreControlledVocabularyItemRestIT() {
    super("/api/v1/");
  }

  private String createControlledVocabularyItem(ObjectStoreControlledVocabularyDto vocabDto) {
    String controlledVocabularyUuid = sendPost(ObjectStoreControlledVocabularyDto.TYPENAME, JsonAPITestHelper.toJsonAPIMap(
      ObjectStoreControlledVocabularyDto.TYPENAME,
      JsonAPITestHelper.toAttributeMap(vocabDto),
      null,
      null)
    ).extract().body().jsonPath().getString("data.id");

    ObjectStoreControlledVocabularyItemDto vocabItemDto = ObjectStoreControlledVocabularyItemTestFixture.newObjectStoreControlledVocabularyItem();
    vocabItemDto.setGroup("dev");

    return sendPost(ObjectStoreControlledVocabularyItemDto.TYPENAME, JsonAPITestHelper.toJsonAPIMap(
      ObjectStoreControlledVocabularyItemDto.TYPENAME,
      JsonAPITestHelper.toAttributeMap(vocabItemDto),
      JsonAPITestHelper.toRelationshipMap(
        JsonAPIRelationship.of("controlledVocabulary", ObjectStoreControlledVocabularyItemDto.TYPENAME, controlledVocabularyUuid)),
      null)
    ).extract().body().jsonPath().getString("data.id");
  }

  @Test
  public void testPost() {
    ObjectStoreControlledVocabularyDto vocabDto = ObjectStoreControlledVocabularyTestFixture.newObjectStoreControlledVocabulary();
    String controlledVocabularyItemUuid = createControlledVocabularyItem(vocabDto);
    sendGet(ObjectStoreControlledVocabularyItemDto.TYPENAME, controlledVocabularyItemUuid);
  }

  
  @Test
  public void resourceUnderTest_whenUpdatingImmutableFields_returnOkAndResourceIsNotUpdated() {
    // Setup: create an resource
    ObjectStoreControlledVocabularyDto vocabDto = ObjectStoreControlledVocabularyTestFixture.newObjectStoreControlledVocabulary();
    String controlledVocabularyItemUuid = createControlledVocabularyItem(vocabDto);
    String originalName = vocabDto.getName();

    vocabDto.setName("updatedName");
    vocabDto.setKey("updatedKey");
    
    // update the resource
    sendPatch(ObjectStoreControlledVocabularyDto.TYPENAME, controlledVocabularyItemUuid, JsonAPITestHelper.toJsonAPIMap(
      ObjectStoreControlledVocabularyDto.TYPENAME, 
      JsonAPITestHelper.toAttributeMap(vocabDto), controlledVocabularyItemUuid));

    ValidatableResponse responseUpdate = sendGet(ObjectStoreControlledVocabularyDto.TYPENAME, controlledVocabularyItemUuid);

    responseUpdate.body("data.attributes.key",
      not(vocabDto.getKey()));

    responseUpdate.body("data.attributes.name",
      equalTo(originalName));

    // cleanup
    sendDelete(ObjectStoreControlledVocabularyDto.TYPENAME, controlledVocabularyItemUuid, HttpStatus.NO_CONTENT.value());
  }

}
