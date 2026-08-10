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

  @Test
  public void testPost() {

    ObjectStoreControlledVocabularyDto vocabDto = ObjectStoreControlledVocabularyTestFixture.newObjectStoreControlledVocabulary();

    String controlledVocabularyUuid = sendPost(ObjectStoreControlledVocabularyDto.TYPENAME, JsonAPITestHelper.toJsonAPIMap(
      ObjectStoreControlledVocabularyDto.TYPENAME,
      JsonAPITestHelper.toAttributeMap(vocabDto),
      null,
      null)
    ).extract().body().jsonPath().getString("data.id");

    ObjectStoreControlledVocabularyItemDto vocabItemDto = ObjectStoreControlledVocabularyItemTestFixture.newObjectStoreControlledVocabularyItem();
    vocabItemDto.setGroup("dev");

    String controlledVocabularyItemUuid = sendPost(ObjectStoreControlledVocabularyItemDto.TYPENAME, JsonAPITestHelper.toJsonAPIMap(
      ObjectStoreControlledVocabularyItemDto.TYPENAME,
      JsonAPITestHelper.toAttributeMap(vocabItemDto),
      JsonAPITestHelper.toRelationshipMap(
        JsonAPIRelationship.of("controlledVocabulary", ObjectStoreControlledVocabularyItemDto.TYPENAME, controlledVocabularyUuid)),
      null)
    ).extract().body().jsonPath().getString("data.id");

    sendGet(ObjectStoreControlledVocabularyItemDto.TYPENAME, controlledVocabularyItemUuid);
  }

}
