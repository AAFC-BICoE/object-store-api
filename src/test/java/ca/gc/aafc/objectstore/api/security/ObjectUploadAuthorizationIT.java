package ca.gc.aafc.objectstore.api.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;

import ca.gc.aafc.dina.exception.ResourceGoneException;
import ca.gc.aafc.dina.exception.ResourceNotFoundException;
import ca.gc.aafc.dina.jsonapi.JsonApiDocument;
import ca.gc.aafc.dina.testsupport.security.WithMockKeycloakUser;
import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.dto.ObjectUploadDto;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.repository.ObjectStoreModuleBaseRepositoryIT;
import ca.gc.aafc.objectstore.api.repository.ObjectUploadResourceRepository;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;
import ca.gc.aafc.objectstore.api.testsupport.fixtures.ObjectUploadTestFixture;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import javax.inject.Inject;

@SpringBootTest(properties = "keycloak.enabled=true")
public class ObjectUploadAuthorizationIT extends BaseIntegrationTest {

  @Inject
  private ObjectUploadResourceRepository repo;

  @Test
  @WithMockKeycloakUser(groupRole = {"CNC:USER"})
  void create_unauthorized_ThrowsAccessDenied() {
    ObjectUploadDto dto = ObjectUploadTestFixture.newObjectUpload();
    JsonApiDocument docToCreate = ObjectStoreModuleBaseRepositoryIT.dtoToJsonApiDocument(dto);
    assertThrows(AccessDeniedException.class, () -> repo.create(docToCreate, null));
  }

  @Test
  @WithMockKeycloakUser(groupRole = {"CNC:USER"})
  void save_unauthorized_ThrowsAccessDenied() {

    UUID uuid = objectUploadService.create(ObjectUploadFactory.buildTestObjectUpload()).getUuid();
    ObjectUploadDto dto = ObjectUploadTestFixture.newObjectUpload();
    dto.setFileIdentifier(uuid);

    JsonApiDocument docToUpdate = ObjectStoreModuleBaseRepositoryIT.dtoToJsonApiDocument(dto);
    assertThrows(AccessDeniedException.class, () -> repo.update(docToUpdate));
  }

  @Test
  @WithMockKeycloakUser(adminRole = {"DINA_ADMIN"})
  void save_isAdmin_Allowed() throws ResourceGoneException, ResourceNotFoundException {
    ObjectUpload testObjectUpload = ObjectUploadFactory.newObjectUpload()
      .build();
    objectUploadService.create(testObjectUpload);

    ObjectUploadDto dto = repo.getOne(testObjectUpload.getUuid(), "").getDto();
    dto.setOriginalFilename("hello.txt");

    JsonApiDocument docToUpdate = ObjectStoreModuleBaseRepositoryIT.dtoToJsonApiDocument(dto);
    repo.update(docToUpdate);
  }
}
