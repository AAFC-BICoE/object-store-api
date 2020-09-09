package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.objectstore.api.dto.ManagedAttributeDto;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.respository.ManagedAttributeResourceRepository;
import ca.gc.aafc.objectstore.api.testsupport.factories.ManagedAttributeFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

@SpringBootTest(properties = "keycloak.enabled=true")
@ActiveProfiles("test")
public class ManagedAttributePermissionServiceIT {

  @Inject
  private ManagedAttributeResourceRepository repoUnderTest;

  @BeforeEach
  void setUp() {
    List<String> expectedGroups = Collections.singletonList("group 1/COLLECTION_MANAGER");
    KeycloakAuthenticationToken mockToken = Mockito.mock(
      KeycloakAuthenticationToken.class,
      Answers.RETURNS_DEEP_STUBS
    );

    mockToken(expectedGroups, mockToken);
    SecurityContextHolder.getContext().setAuthentication(mockToken);
  }

  @Test
  void name() {
    ManagedAttribute managedAttribute = ManagedAttributeFactory.newManagedAttribute().build();
    ManagedAttributeDto dto = new ManagedAttributeDto();
    dto.setUuid(managedAttribute.getUuid());
    dto.setName(managedAttribute.getName());
    dto.setDescription(managedAttribute.getDescription());
    dto.setManagedAttributeType(managedAttribute.getManagedAttributeType());
    repoUnderTest.create(dto);

    Assertions.assertTrue(true);
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