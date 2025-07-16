package ca.gc.aafc.objectstore.api.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;

import ca.gc.aafc.dina.exception.ResourceGoneException;
import ca.gc.aafc.dina.exception.ResourceNotFoundException;
import ca.gc.aafc.dina.jsonapi.JsonApiDocument;
import ca.gc.aafc.dina.jsonapi.JsonApiDocuments;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.dina.testsupport.security.WithMockKeycloakUser;
import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreManagedAttributeDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreManagedAttribute;
import ca.gc.aafc.objectstore.api.repository.ObjectStoreManagedAttributeResourceRepository;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreManagedAttributeFactory;
import ca.gc.aafc.objectstore.api.testsupport.fixtures.ObjectStoreManagedAttributeFixture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.inject.Inject;

@SpringBootTest(properties = "keycloak.enabled=true")
public class ManagedAttributeAuthorizationIT extends BaseIntegrationTest {

  @Inject
  private ObjectStoreManagedAttributeResourceRepository repoUnderTest;

  /** An existing managed attribute in the database. */
  private ObjectStoreManagedAttribute managedAttribute;

  @BeforeEach
  public void persistManagedAttribute() {
    managedAttribute = ObjectStoreManagedAttributeFactory.newManagedAttribute()
      .createdBy("test-method")
      .build();
    managedAttributeService.create(managedAttribute);
  }

  @WithMockKeycloakUser(groupRole = {"group 1:USER"})
  @Test
  void create_unauthorizedUser_ThrowsAccessDenied() {
    JsonApiDocument docToCreate = JsonApiDocuments.createJsonApiDocument(
      null, ObjectStoreManagedAttributeDto.TYPENAME,
      JsonAPITestHelper.toAttributeMap(new ObjectStoreManagedAttributeDto())
    );

    assertThrows(AccessDeniedException.class,
      () -> repoUnderTest.create(docToCreate, null));
  }

  @WithMockKeycloakUser(groupRole = {"group 1:SUPER_USER"})
  @Test
  void create_authorizedUser_DoesNotThrowAccessDenied() {

    JsonApiDocument docToCreate = JsonApiDocuments.createJsonApiDocument(
      null, ObjectStoreManagedAttributeDto.TYPENAME,
      JsonAPITestHelper.toAttributeMap(ObjectStoreManagedAttributeFixture.newObjectStoreManagedAttribute())
    );
    assertDoesNotThrow(() -> repoUnderTest.create(docToCreate, null));
  }

  @WithMockKeycloakUser(groupRole = {"group 1:DINA_ADMIN"})
  @Test
  void create_Admin_DoesNotThrowAccessDenied() {
    JsonApiDocument docToCreate = JsonApiDocuments.createJsonApiDocument(
      null, ObjectStoreManagedAttributeDto.TYPENAME,
      JsonAPITestHelper.toAttributeMap(ObjectStoreManagedAttributeFixture.newObjectStoreManagedAttribute())
    );

    assertDoesNotThrow(() -> repoUnderTest.create(docToCreate, null));
  }

  @WithMockKeycloakUser(groupRole = {"group 1:USER"})
  @Test
  void delete_unauthorizedUser_ThrowsAccessDeniedException()
    throws ResourceGoneException, ResourceNotFoundException {
    assertNotNull(repoUnderTest.onFindOne(managedAttribute.getUuid().toString(), null));
    assertThrows(AccessDeniedException.class, () -> repoUnderTest.delete(managedAttribute.getUuid()));
  }

  @WithMockKeycloakUser(groupRole = {"group 1:SUPER_USER"})
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
      dto.getUuid(), ObjectStoreManagedAttributeDto.TYPENAME,
      JsonAPITestHelper.toAttributeMap(dto)
    );

    assertThrows(AccessDeniedException.class, () -> repoUnderTest.onUpdate(docToUpdate, dto.getUuid()));
  }

  @WithMockKeycloakUser(groupRole = {"group 1:SUPER_USER"})
  @Test
  void update_authorizedUser_DoesNotThrowAccessDenied()
    throws ResourceGoneException, ResourceNotFoundException {
    JsonApiDocument docToCreate = JsonApiDocuments.createJsonApiDocument(
      null, ObjectStoreManagedAttributeDto.TYPENAME,
      JsonAPITestHelper.toAttributeMap(ObjectStoreManagedAttributeFixture.newObjectStoreManagedAttribute())
    );

    ObjectStoreManagedAttributeDto dto = repoUnderTest.create(docToCreate, null).getDto();

    JsonApiDocument docToUpdate = JsonApiDocuments.createJsonApiDocument(
      dto.getUuid(), ObjectStoreManagedAttributeDto.TYPENAME,
      JsonAPITestHelper.toAttributeMap(dto)
    );

    ObjectStoreManagedAttributeDto persistedDto = repoUnderTest.getOne(
      dto.getUuid(), null).getDto();
    assertDoesNotThrow(() -> repoUnderTest.onUpdate(docToUpdate, dto.getUuid()));
  }

}
