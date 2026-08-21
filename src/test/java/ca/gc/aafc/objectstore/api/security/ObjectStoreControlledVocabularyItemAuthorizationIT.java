package ca.gc.aafc.objectstore.api.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;

import ca.gc.aafc.dina.exception.ResourceGoneException;
import ca.gc.aafc.dina.exception.ResourceNotFoundException;
import ca.gc.aafc.dina.jsonapi.JsonApiDocument;
import ca.gc.aafc.dina.jsonapi.JsonApiDocuments;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPIRelationship;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.dina.testsupport.security.WithMockKeycloakUser;
import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.config.ObjectStoreVocabularyConfiguration;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreControlledVocabularyItemDto;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreControlledVocabularyDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreControlledVocabularyItem;
import ca.gc.aafc.objectstore.api.repository.ObjectStoreControlledVocabularyItemRepository;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreControlledVocabularyItemTestFactory;
import ca.gc.aafc.objectstore.api.testsupport.fixtures.ObjectStoreControlledVocabularyItemTestFixture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import jakarta.inject.Inject;

@SpringBootTest(properties = "keycloak.enabled=true")
public class ObjectStoreControlledVocabularyItemAuthorizationIT extends BaseIntegrationTest {

  private static final String GROUP_IN_TEST = "group 1";

  @Inject
  private ObjectStoreControlledVocabularyItemRepository repoUnderTest;

  /** An existing managed attribute in the database. */
  private ObjectStoreControlledVocabularyItem managedAttribute;

  @BeforeEach
  public void persistManagedAttribute() {
     managedAttribute = ObjectStoreControlledVocabularyItemTestFactory.newObjectStoreControlledVocabularyItem()
      .createdBy("test-method")
      .group(GROUP_IN_TEST)
      .controlledVocabulary(getManagedAttributeControlledVocabularyRef())
      .build();
    controlledVocabularyItemService.create(managedAttribute);
  }

  @WithMockKeycloakUser(groupRole = {"group 1:USER"})
  @Test
  void create_unauthorizedUser_ThrowsAccessDenied() {
    JsonApiDocument docToCreate = JsonApiDocuments.createJsonApiDocument(
      null, ObjectStoreControlledVocabularyItemDto.TYPENAME,
      JsonAPITestHelper.toAttributeMap(new ObjectStoreControlledVocabularyItemDto())
    );

    assertThrows(AccessDeniedException.class,
      () -> repoUnderTest.create(docToCreate, null));
  }

  @WithMockKeycloakUser(groupRole = {GROUP_IN_TEST + ":SUPER_USER"})
  @Test
  void create_authorizedUser_DoesNotThrowAccessDenied() {
     JsonApiDocument docToCreate = JsonApiDocuments.createJsonApiDocumentWithRelToOne(
        null, ObjectStoreControlledVocabularyItemDto.TYPENAME,
        JsonAPITestHelper.toAttributeMap(
            ObjectStoreControlledVocabularyItemTestFixture.newObjectStoreControlledVocabularyItem(GROUP_IN_TEST)),
        Map.of("controlledVocabulary",
            JsonApiDocument.ResourceIdentifier.builder().type(ObjectStoreControlledVocabularyDto.TYPENAME)
                .id(ObjectStoreVocabularyConfiguration.MANAGED_ATTRIBUTE_VOCAB_UUID).build()));
    assertDoesNotThrow(() -> repoUnderTest.create(docToCreate, null));
  }

  @WithMockKeycloakUser(adminRole = { "DINA_ADMIN" })
  @Test
  void create_Admin_DoesNotThrowAccessDenied() {
    JsonApiDocument docToCreate = JsonApiDocuments.createJsonApiDocumentWithRelToOne(
        null, ObjectStoreControlledVocabularyItemDto.TYPENAME,
        JsonAPITestHelper.toAttributeMap(
            ObjectStoreControlledVocabularyItemTestFixture.newObjectStoreControlledVocabularyItem(GROUP_IN_TEST)),
        Map.of("controlledVocabulary",
            JsonApiDocument.ResourceIdentifier.builder().type(ObjectStoreControlledVocabularyDto.TYPENAME)
                .id(ObjectStoreVocabularyConfiguration.MANAGED_ATTRIBUTE_VOCAB_UUID).build()));

    assertDoesNotThrow(() -> repoUnderTest.create(docToCreate, null));
  }

  @WithMockKeycloakUser(groupRole = {"group 1:USER"})
  @Test
  void delete_unauthorizedUser_ThrowsAccessDeniedException()
    throws ResourceGoneException, ResourceNotFoundException {
    assertNotNull(repoUnderTest.onFindOne(managedAttribute.getUuid().toString(), null));
    assertThrows(AccessDeniedException.class, () -> repoUnderTest.delete(managedAttribute.getUuid()));
  }

  @WithMockKeycloakUser(groupRole = {GROUP_IN_TEST + ":SUPER_USER"})
  @Test
  void delete_authorizedUser_DoesNotThrowAccessDenied()
    throws ResourceGoneException, ResourceNotFoundException {
    assertNotNull(repoUnderTest.onFindOne(managedAttribute.getUuid().toString(), null));
    assertDoesNotThrow(
      () -> repoUnderTest.delete(managedAttribute.getUuid()));
  }

  @WithMockKeycloakUser(groupRole = {"group 1:USER"})
  @Test
  void update_unauthorizedUser_ThrowAccessDenied()
    throws ResourceGoneException, ResourceNotFoundException {
    var dto = repoUnderTest.getOne(managedAttribute.getUuid(), null).getDto();

    JsonApiDocument docToUpdate = JsonApiDocuments.createJsonApiDocument(
      dto.getUuid(), ObjectStoreControlledVocabularyItemDto.TYPENAME,
      JsonAPITestHelper.toAttributeMap(dto)
    );

    assertThrows(AccessDeniedException.class, () -> repoUnderTest.onUpdate(docToUpdate, dto.getUuid()));
  }

  @WithMockKeycloakUser(groupRole = {GROUP_IN_TEST + ":SUPER_USER"})
  @Test
  void update_authorizedUser_DoesNotThrowAccessDenied() throws ResourceGoneException, ResourceNotFoundException {
    JsonApiDocument docToCreate = JsonApiDocuments.createJsonApiDocumentWithRelToOne(
        null, ObjectStoreControlledVocabularyItemDto.TYPENAME,
        JsonAPITestHelper.toAttributeMap(
            ObjectStoreControlledVocabularyItemTestFixture.newObjectStoreControlledVocabularyItem(GROUP_IN_TEST)),
        Map.of("controlledVocabulary",
            JsonApiDocument.ResourceIdentifier.builder().type(ObjectStoreControlledVocabularyDto.TYPENAME)
                .id(ObjectStoreVocabularyConfiguration.MANAGED_ATTRIBUTE_VOCAB_UUID).build()));
    ObjectStoreControlledVocabularyItemDto dto = repoUnderTest.create(docToCreate, null).getDto();

    JsonApiDocument docToUpdate = JsonApiDocuments.createJsonApiDocument(
      dto.getUuid(), ObjectStoreControlledVocabularyItemDto.TYPENAME,
      JsonAPITestHelper.toAttributeMap(dto)
    );

    ObjectStoreControlledVocabularyItemDto persistedDto = repoUnderTest.getOne(
      dto.getUuid(), null).getDto();
    assertDoesNotThrow(() -> repoUnderTest.onUpdate(docToUpdate, dto.getUuid()));
  }

}
