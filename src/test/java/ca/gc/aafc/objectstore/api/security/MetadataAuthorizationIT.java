package ca.gc.aafc.objectstore.api.security;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;

import ca.gc.aafc.dina.exception.ResourceGoneException;
import ca.gc.aafc.dina.exception.ResourceNotFoundException;
import ca.gc.aafc.dina.jsonapi.JsonApiDocument;
import ca.gc.aafc.dina.testsupport.security.WithMockKeycloakUser;
import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.repository.ObjectStoreMetadataRepositoryV2;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;

import io.minio.MinioClient;
import java.util.UUID;
import javax.inject.Inject;

import static ca.gc.aafc.objectstore.api.repository.ObjectStoreModuleBaseRepositoryIT.dtoToJsonApiDocument;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = "keycloak.enabled=true")
public class MetadataAuthorizationIT extends BaseIntegrationTest {

  @MockBean
  private MinioClient minioClient;

  @Inject
  private ObjectStoreMetadataRepositoryV2 repo;

  private static final String GROUP_1 = "CNC";
  private static final String TEST_USAGE_TERMS = "test user terms";
  public ObjectUpload testObjectUpload;
  private ObjectStoreMetadata persisted;

  @BeforeEach
  void setUp() {
    testObjectUpload = ObjectUploadFactory.newObjectUpload().build();
    testObjectUpload.setDcType(DcType.TEXT);
    testObjectUpload.setEvaluatedMediaType(MediaType.TEXT_PLAIN_VALUE);
    objectUploadService.create(testObjectUpload);
    persisted = persistMeta(GROUP_1);
  }

  @AfterEach
  void tearDown() {
    objectUploadService.delete(testObjectUpload);
    repo.getAll("")
      .resourceList()
      .forEach(m -> objectStoreMetaDataService.delete(
        objectStoreMetaDataService.findOne(
          m.getDto().getUuid(), ObjectStoreMetadata.class)));
  }

  @Test
  @WithMockKeycloakUser(groupRole = {"CNC:USER"})
  void create_Unauthorized_ThrowsAccessDenied() {
    ObjectStoreMetadataDto dto = newMetaDto("invalidGroup");
    JsonApiDocument docToCreate = dtoToJsonApiDocument(dto);

    assertThrows(AccessDeniedException.class, () -> repo.create(docToCreate, null));
  }

  @Test
  @WithMockKeycloakUser(groupRole = {"CNC:USER"})
  void create_Authorized_RecordCreated() {
    ObjectStoreMetadataDto dto = newMetaDto(GROUP_1);
    JsonApiDocument docToCreate = dtoToJsonApiDocument(dto);
    UUID uuid = repo.create(docToCreate, null).getDto().getJsonApiId();
    Assertions.assertNotNull(uuid);
  }

  @Test
  @WithMockKeycloakUser(groupRole = {"Invalid:USER"})
  public void save_UnAuthorizedGroup_ThrowsAccessDeniedException() throws ResourceGoneException, ResourceNotFoundException {
    ObjectStoreMetadataDto toUpdate = repo.getOne(persisted.getUuid(), "").getDto();

    toUpdate.setAcCaption("new");
    JsonApiDocument docToUpdate = dtoToJsonApiDocument(toUpdate);
    assertThrows(AccessDeniedException.class, () -> repo.update(docToUpdate));
  }

  @Test
  @WithMockKeycloakUser(groupRole = {"CNC:USER"})
  public void save_AuthorizedGroup_UpdatesObject() throws ResourceGoneException, ResourceNotFoundException {
    String expected = "new value";
    ObjectStoreMetadataDto toUpdate = repo.getOne(persisted.getUuid(), "").getDto();
    toUpdate.setAcCaption(expected);

    JsonApiDocument docToUpdate = dtoToJsonApiDocument(toUpdate);
    ObjectStoreMetadataDto result = repo.update(docToUpdate).getDto();
    assertEquals(expected, result.getAcCaption());
  }

  @Test
  @WithMockKeycloakUser(groupRole = {"CNC:USER"})
  public void delete_NotSuperUser_ThrowsAccessDeniedException() {
    ObjectStoreMetadataDto dto = newMetaDto(GROUP_1);
    JsonApiDocument docToCreate = dtoToJsonApiDocument(dto);

    ObjectStoreMetadataDto createdDto = repo.create(docToCreate, null).getDto();
    assertThrows(AccessDeniedException.class, () -> repo.delete(createdDto.getUuid()));
  }

  @Test
  @WithMockKeycloakUser(groupRole = {"CNC:SUPER_USER"})
  public void delete_SuperUser_resourceDeleted() throws ResourceGoneException, ResourceNotFoundException {
    ObjectStoreMetadataDto dto = newMetaDto(GROUP_1);
    JsonApiDocument docToCreate = dtoToJsonApiDocument(dto);
    ObjectStoreMetadataDto createdDto = repo.create(docToCreate, null).getDto();

    repo.delete(createdDto.getUuid());
    assertThrows(ResourceGoneException.class, () -> repo.getOne(createdDto.getUuid(), ""));
  }

  @Test
  @WithMockKeycloakUser(groupRole = {"Invalid:USER"})
  public void delete_UnAuthorizedGroup_ThrowsAccessDeniedException() {
    ObjectStoreMetadata group1 = persistMeta("Group1");
    assertThrows(AccessDeniedException.class, () -> repo.delete(group1.getUuid()));
  }

  private ObjectStoreMetadataDto newMetaDto(String group) {
    ObjectStoreMetadataDto meta = new ObjectStoreMetadataDto();
    meta.setFileIdentifier(testObjectUpload.getFileIdentifier());
    meta.setDcType(DcType.IMAGE);
    meta.setBucket(group.toLowerCase());
    meta.setXmpRightsUsageTerms(TEST_USAGE_TERMS);
    meta.setCreatedBy(RandomStringUtils.random(4));
    return meta;
  }

  private ObjectStoreMetadata persistMeta(String group) {
    ObjectUpload objectUpload = ObjectUploadFactory.newObjectUpload().build();
    objectUploadService.create(objectUpload);
    ObjectStoreMetadata meta = ObjectStoreMetadataFactory.newObjectStoreMetadata().build();
    meta.setBucket(group.toLowerCase());
    meta.setFileIdentifier(objectUpload.getFileIdentifier());
    objectStoreMetaDataService.create(meta);
    return meta;
  }

}
