package ca.gc.aafc.objectstore.api.security;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;

import ca.gc.aafc.dina.testsupport.security.WithMockKeycloakUser;
import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.dto.ObjectUploadDto;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.repository.ObjectUploadResourceRepository;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;
import ca.gc.aafc.objectstore.api.testsupport.fixtures.ObjectUploadTestFixture;

import io.crnk.core.queryspec.QuerySpec;
import javax.inject.Inject;

@SpringBootTest(properties = "keycloak.enabled=true")
public class ObjectUploadAuthorizationIT extends BaseIntegrationTest {

  @Inject
  private ObjectUploadResourceRepository repo;

  @Test
  @WithMockKeycloakUser(groupRole = {"CNC:USER"})
  void create_unauthorized_ThrowsAccessDenied() {
    ObjectUploadDto dto = ObjectUploadTestFixture.newObjectUpload();
    Assertions.assertThrows(AccessDeniedException.class, () -> repo.create(dto));
  }

  @Test
  @WithMockKeycloakUser(groupRole = {"CNC:USER"})
  void save_unauthorized_ThrowsAccessDenied() {
    ObjectUploadDto dto = ObjectUploadTestFixture.newObjectUpload();
    Assertions.assertThrows(AccessDeniedException.class, () -> repo.save(dto));
  }

  @Test
  @WithMockKeycloakUser(adminRole = {"DINA_ADMIN"})
  void save_isAdmin_Allowed() {
    ObjectUpload testObjectUpload = ObjectUploadFactory.newObjectUpload()
      .build();
    objectUploadService.create(testObjectUpload);

    ObjectUploadDto dto = repo.findOne(testObjectUpload.getUuid(), new QuerySpec(ObjectUploadDto.class));
    dto.setOriginalFilename("hello.txt");
    repo.save(dto);
  }
}
