package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.dina.testsupport.security.WithMockKeycloakUser;
import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreManagedAttributeDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreManagedAttribute;
import ca.gc.aafc.objectstore.api.repository.ObjectStoreManagedAttributeResourceRepository;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreManagedAttributeFactory;
import ca.gc.aafc.objectstore.api.testsupport.fixtures.ObjectStoreManagedAttributeFixture;
import io.crnk.core.queryspec.QuerySpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;

import javax.inject.Inject;

@SpringBootTest(properties = "keycloak.enabled=true")
public class ObjectStoreManagedAttributePermissionServiceIT extends BaseIntegrationTest {

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
    Assertions.assertThrows(
      AccessDeniedException.class,
      () -> repoUnderTest.create(new ObjectStoreManagedAttributeDto()));
  }

  @WithMockKeycloakUser(groupRole = {"group 1:SUPER_USER"})
  @Test
  void create_authorizedUser_DoesNotThrowAccessDenied() {
    Assertions.assertDoesNotThrow(() -> repoUnderTest.create(ObjectStoreManagedAttributeFixture.newObjectStoreManagedAttribute()));
  }

  @WithMockKeycloakUser(groupRole = {"group 1:DINA_ADMIN"})
  @Test
  void create_Admin_DoesNotThrowAccessDenied() {
    Assertions.assertDoesNotThrow(() -> repoUnderTest.create(ObjectStoreManagedAttributeFixture.newObjectStoreManagedAttribute()));
  }

  @WithMockKeycloakUser(groupRole = {"group 1:USER"})
  @Test
  void delete_unauthorizedUser_ThrowsAccessDeniedException() {
    Assertions.assertNotNull(repoUnderTest.findOne(managedAttribute.getUuid(), new QuerySpec(ObjectStoreManagedAttributeDto.class)));
    Assertions.assertThrows(AccessDeniedException.class, () -> repoUnderTest.delete(managedAttribute.getUuid()));
  }

  @WithMockKeycloakUser(groupRole = {"group 1:SUPER_USER"})
  @Test
  void delete_authorizedUser_DoesNotThrowAccessDenied() {
    Assertions.assertNotNull(repoUnderTest.findOne(managedAttribute.getUuid(), new QuerySpec(ObjectStoreManagedAttributeDto.class)));
    Assertions.assertDoesNotThrow(
      () -> repoUnderTest.delete(managedAttribute.getUuid()));
  }

  @WithMockKeycloakUser(groupRole = {"group 1:USER"})
  @Test
  void update_unauthorizedUser_ThrowAccessDenied() {
    var dto = repoUnderTest.findOne(managedAttribute.getUuid(), new QuerySpec(ObjectStoreManagedAttributeDto.class));
 
    Assertions.assertNotNull(dto);
    Assertions.assertThrows(AccessDeniedException.class, () -> repoUnderTest.save(dto));
  }

  @WithMockKeycloakUser(groupRole = {"group 1:SUPER_USER"})
  @Test
  void update_authorizedUser_DoesNotThrowAccessDenied() {
    ObjectStoreManagedAttributeDto dto = repoUnderTest.create(ObjectStoreManagedAttributeFixture.newObjectStoreManagedAttribute());

    ObjectStoreManagedAttributeDto persistedDto = repoUnderTest.findOne(
      dto.getUuid(),
      new QuerySpec(ObjectStoreManagedAttributeDto.class));
    Assertions.assertDoesNotThrow(() -> repoUnderTest.save(persistedDto));
  }

}
