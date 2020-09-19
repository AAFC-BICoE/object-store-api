package ca.gc.aafc.objectstore.api.service;


import ca.gc.aafc.dina.testsupport.security.WithMockKeycloakUser;
import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.dto.ManagedAttributeDto;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.respository.ManagedAttributeResourceRepository;
import com.google.common.collect.ImmutableMap;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;

import javax.inject.Inject;

@SpringBootTest(properties = "keycloak.enabled=true")
public class ManagedAttributePermissionServiceIT extends BaseIntegrationTest {

  @Inject
  private ManagedAttributeResourceRepository repoUnderTest;

  /** An existing managed attribute in the database. */
  private ManagedAttribute managedAttribute;

  @BeforeEach
  public void persistManagedAttribute() {
    managedAttribute = ManagedAttribute.builder()
      .name(RandomStringUtils.randomAlphabetic(4))
      .description(ImmutableMap.of("en", "Test"))
      .managedAttributeType(ManagedAttribute.ManagedAttributeType.STRING)
      .createdBy("test-method")
      .build();
    service.save(managedAttribute);
  }

  @WithMockKeycloakUser(groupRole = {"group 1:STAFF"})
  @Test
  void create_unauthorizedUser_ThrowsAccessDenied() {
    Assertions.assertThrows(
      AccessDeniedException.class,
      () -> repoUnderTest.create(new ManagedAttributeDto()));
  }

  @WithMockKeycloakUser(groupRole = {"group 1:COLLECTION_MANAGER"})
  @Test
  void create_authorizedUser_DoesNotThrowAccessDenied() {
    Assertions.assertDoesNotThrow(() -> repoUnderTest.create(createDto()));
  }

  @WithMockKeycloakUser(groupRole = {"group 1:STAFF"})
  @Test
  void delete_unauthorizedUser_ThrowAccessDenied() {
    Assertions.assertNotNull(repoUnderTest.findOne(managedAttribute.getUuid(), new QuerySpec(ManagedAttributeDto.class)));
    Assertions.assertThrows(AccessDeniedException.class, () -> repoUnderTest.delete(managedAttribute.getUuid()));
  }

  @WithMockKeycloakUser(groupRole = {"group 1:COLLECTION_MANAGER"})
  @Test
  void delete_authorizedUser_DoesNotThrowAccessDenied() {
    Assertions.assertNotNull(repoUnderTest.findOne(managedAttribute.getUuid(), new QuerySpec(ManagedAttributeDto.class)));
    Assertions.assertDoesNotThrow(() -> repoUnderTest.delete(managedAttribute.getUuid()));
    Assertions.assertThrows(
      ResourceNotFoundException.class,
      () -> repoUnderTest.findOne(managedAttribute.getUuid(), new QuerySpec(ManagedAttributeDto.class)));
  }

  @WithMockKeycloakUser(groupRole = {"group 1:STAFF"})
  @Test
  void update_unauthorizedUser_ThrowAccessDenied() {
    var dto = repoUnderTest.findOne(managedAttribute.getUuid(), new QuerySpec(ManagedAttributeDto.class));
 
    Assertions.assertNotNull(dto);
    Assertions.assertThrows(AccessDeniedException.class, () -> repoUnderTest.save(dto));
  }

  @WithMockKeycloakUser(groupRole = {"group 1:COLLECTION_MANAGER"})
  @Test
  void update_authorizedUser_DoesNotThrowAccessDenied() {
    ManagedAttributeDto dto = repoUnderTest.create(createDto());

    ManagedAttributeDto persistedDto = repoUnderTest.findOne(
      dto.getUuid(),
      new QuerySpec(ManagedAttributeDto.class));
    Assertions.assertDoesNotThrow(() -> repoUnderTest.save(persistedDto));
  }

  private static ManagedAttributeDto createDto() {
    ManagedAttributeDto dto = new ManagedAttributeDto();
    dto.setName(RandomStringUtils.randomAlphabetic(4));
    dto.setDescription(ImmutableMap.of("en", "Test"));
    dto.setManagedAttributeType(ManagedAttribute.ManagedAttributeType.STRING);
    return dto;
  }

}
