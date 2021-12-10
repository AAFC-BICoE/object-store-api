package ca.gc.aafc.objectstore.api.service;


import ca.gc.aafc.dina.i18n.MultilingualDescription;
import ca.gc.aafc.dina.testsupport.security.WithMockKeycloakUser;
import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreManagedAttributeDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreManagedAttribute;
import ca.gc.aafc.objectstore.api.repository.ObjectStoreManagedAttributeResourceRepository;
import ca.gc.aafc.objectstore.api.testsupport.factories.MultilingualDescriptionFactory;
import io.crnk.core.queryspec.QuerySpec;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

import javax.inject.Inject;

@SpringBootTest(properties = "keycloak.enabled=true")
public class ObjectStoreManagedAttributePermissionServiceIT extends BaseIntegrationTest {

  @Inject
  private ObjectStoreManagedAttributeResourceRepository repoUnderTest;

  /** An existing managed attribute in the database. */
  private ObjectStoreManagedAttribute managedAttribute;

  @BeforeEach
  public void persistManagedAttribute() {
    managedAttribute = ObjectStoreManagedAttribute.builder()
      .name(RandomStringUtils.randomAlphabetic(4))
      .multilingualDescription(MultilingualDescriptionFactory.newMultilingualDescription().build())
      .managedAttributeType(ObjectStoreManagedAttribute.ManagedAttributeType.STRING)
      .createdBy("test-method")
      .build();
    managedAttributeService.create(managedAttribute);
  }

  @WithMockKeycloakUser(groupRole = {"group 1:STAFF"})
  @Test
  void create_unauthorizedUser_ThrowsAccessDenied() {
    Assertions.assertThrows(
      AccessDeniedException.class,
      () -> repoUnderTest.create(new ObjectStoreManagedAttributeDto()));
  }

  @WithMockKeycloakUser(groupRole = {"group 1:COLLECTION_MANAGER"})
  @Test
  void create_authorizedUser_DoesNotThrowAccessDenied() {
    Assertions.assertDoesNotThrow(() -> repoUnderTest.create(createDto()));
  }

  @WithMockKeycloakUser(groupRole = {"group 1:STAFF"})
  @Test
  void delete_unauthorizedUser_ThrowsAccessDeniedException() {
    Assertions.assertNotNull(repoUnderTest.findOne(managedAttribute.getUuid(), new QuerySpec(ObjectStoreManagedAttributeDto.class)));
    Assertions.assertThrows(AccessDeniedException.class, () -> repoUnderTest.delete(managedAttribute.getUuid()));
  }

  @WithMockKeycloakUser(groupRole = {"group 1:COLLECTION_MANAGER"})
  @Test
  void delete_authorizedUser_DoesNotThrowAccessDenied() {
    Assertions.assertNotNull(repoUnderTest.findOne(managedAttribute.getUuid(), new QuerySpec(ObjectStoreManagedAttributeDto.class)));
    Assertions.assertDoesNotThrow(
      () -> repoUnderTest.delete(managedAttribute.getUuid()));
  }

  @WithMockKeycloakUser(groupRole = {"group 1:STAFF"})
  @Test
  void update_unauthorizedUser_ThrowAccessDenied() {
    var dto = repoUnderTest.findOne(managedAttribute.getUuid(), new QuerySpec(ObjectStoreManagedAttributeDto.class));
 
    Assertions.assertNotNull(dto);
    Assertions.assertThrows(AccessDeniedException.class, () -> repoUnderTest.save(dto));
  }

  @WithMockKeycloakUser(groupRole = {"group 1:COLLECTION_MANAGER"})
  @Test
  void update_authorizedUser_DoesNotThrowAccessDenied() {
    ObjectStoreManagedAttributeDto dto = repoUnderTest.create(createDto());

    ObjectStoreManagedAttributeDto persistedDto = repoUnderTest.findOne(
      dto.getUuid(),
      new QuerySpec(ObjectStoreManagedAttributeDto.class));
    Assertions.assertDoesNotThrow(() -> repoUnderTest.save(persistedDto));
  }

  private static ObjectStoreManagedAttributeDto createDto() {
    ObjectStoreManagedAttributeDto dto = new ObjectStoreManagedAttributeDto();
    dto.setName(RandomStringUtils.randomAlphabetic(4));
    dto.setMultilingualDescription(MultilingualDescriptionFactory.newMultilingualDescription().build());
    dto.setManagedAttributeType(ObjectStoreManagedAttribute.ManagedAttributeType.STRING);
    return dto;
  }

}
