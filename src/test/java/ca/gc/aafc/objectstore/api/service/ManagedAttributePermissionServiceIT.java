package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import ca.gc.aafc.dina.security.DinaRole;
import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.dto.ManagedAttributeDto;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.respository.ManagedAttributeResourceRepository;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@SpringBootTest(properties = "keycloak.enabled=true")

public class ManagedAttributePermissionServiceIT extends BaseIntegrationTest {

  @Inject
  private ManagedAttributeResourceRepository repoUnderTest;

  @MockBean
  private DinaAuthenticatedUser currentUser;

  @BeforeEach
  void setUp() {
    /* A valid authentication token is required in the security context,
    however it is the mocked current user which defines the current role
    of the user for the tests */
    SecurityContextHolder.getContext().setAuthentication(createMockedToken());
  }

  @AfterAll
  static void afterAll() {
    // Prevent other tests from failing from the mocked security context.
    SecurityContextHolder.getContext().setAuthentication(null);
  }

  @Test
  void create_unauthorizedUser_ThrowsAccessDenied() {
    mockCurrentUser(DinaRole.STAFF);
    Assertions.assertThrows(
      AccessDeniedException.class,
      () -> repoUnderTest.create(new ManagedAttributeDto()));
  }

  @Test
  void create_authorizedUser_DoesNotThrowAccessDenied() {
    mockCurrentUser(DinaRole.COLLECTION_MANAGER);
    Assertions.assertDoesNotThrow(() -> repoUnderTest.create(createDto()));
  }

  @Test
  void delete_unauthorizedUser_ThrowAccessDenied() {
    mockCurrentUser(DinaRole.COLLECTION_MANAGER);
    UUID id = repoUnderTest.create(createDto()).getUuid();

    mockCurrentUser(DinaRole.STAFF);
    Assertions.assertNotNull(repoUnderTest.findOne(id, new QuerySpec(ManagedAttributeDto.class)));
    Assertions.assertThrows(AccessDeniedException.class, () -> repoUnderTest.delete(id));
  }

  @Test
  void delete_authorizedUser_DoesNotThrowAccessDenied() {
    mockCurrentUser(DinaRole.COLLECTION_MANAGER);
    UUID id = repoUnderTest.create(createDto()).getUuid();

    Assertions.assertNotNull(repoUnderTest.findOne(id, new QuerySpec(ManagedAttributeDto.class)));
    Assertions.assertDoesNotThrow(() -> repoUnderTest.delete(id));
    Assertions.assertThrows(
      ResourceNotFoundException.class,
      () -> repoUnderTest.findOne(id, new QuerySpec(ManagedAttributeDto.class)));
  }

  @Test
  void update_unauthorizedUser_ThrowAccessDenied() {
    mockCurrentUser(DinaRole.COLLECTION_MANAGER);
    ManagedAttributeDto dto = repoUnderTest.create(createDto());

    mockCurrentUser(DinaRole.STAFF);
    Assertions.assertNotNull(
      repoUnderTest.findOne(dto.getUuid(), new QuerySpec(ManagedAttributeDto.class)));
    Assertions.assertThrows(AccessDeniedException.class, () -> repoUnderTest.save(dto));
  }

  @Test
  void update_authorizedUser_DoesNotThrowAccessDenied() {
    mockCurrentUser(DinaRole.COLLECTION_MANAGER);
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

  private void mockCurrentUser(DinaRole role) {
    Mockito.when(currentUser.getRolesPerGroup())
      .thenReturn(ImmutableMap.of("group1", ImmutableSet.of(role)));
    Mockito.when(currentUser.getUsername()).thenReturn("test user");
  }

  private static KeycloakAuthenticationToken createMockedToken() {
    List<String> expectedGroups = Collections.singletonList("group 1/COLLECTION_MANAGER");
    KeycloakAuthenticationToken mockToken = Mockito.mock(
      KeycloakAuthenticationToken.class,
      Answers.RETURNS_DEEP_STUBS
    );

    mockToken(expectedGroups, mockToken);
    return mockToken;
  }

  public static void mockToken(
    List<String> keycloakGroupClaim,
    KeycloakAuthenticationToken mockToken
  ) {
    Mockito.when(mockToken.getName()).thenReturn("test-user");
    mockClaim(mockToken, "agent-identifier", "a2cef694-10f1-42ec-b403-e0f8ae9d2ae6");
    mockClaim(mockToken, "groups", keycloakGroupClaim);
  }

  public static void mockClaim(KeycloakAuthenticationToken token, String key, Object value) {
    Mockito.when(
      token.getAccount()
        .getKeycloakSecurityContext()
        .getToken()
        .getOtherClaims()
        .get(key))
      .thenReturn(value);
  }
}
