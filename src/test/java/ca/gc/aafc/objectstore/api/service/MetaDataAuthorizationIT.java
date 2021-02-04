package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.dina.repository.GoneException;
import ca.gc.aafc.dina.testsupport.security.WithMockKeycloakUser;
import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.MinioTestConfiguration;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.respository.ObjectStoreResourceRepository;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;
import io.crnk.core.queryspec.QuerySpec;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;

import javax.inject.Inject;
import java.util.UUID;

@SpringBootTest(properties = "keycloak.enabled=true")
public class MetaDataAuthorizationIT extends BaseIntegrationTest {

  @Inject
  private ObjectStoreResourceRepository repo;
  private static final String GROUP_1 = "CNC";
  public ObjectUpload testObjectUpload;
  private ObjectStoreMetadata persisted;

  @BeforeEach
  void setUp() {
    testObjectUpload = ObjectUploadFactory.newObjectUpload().build();
    testObjectUpload.setDcType(DcType.TEXT);
    service.save(testObjectUpload);
    persisted = persistMeta(GROUP_1);
  }

  @AfterEach
  void tearDown() {
    service.deleteById(ObjectUpload.class, testObjectUpload.getId());
    repo.findAll(new QuerySpec(ObjectStoreMetadataDto.class))
      .forEach(m -> service.deleteByProperty(ObjectStoreMetadata.class, "uuid", m.getUuid()));
  }

  @Test
  @WithMockKeycloakUser(groupRole = {"CNC:STAFF"})
  void create_Unauthorized_ThrowsAccessDenied() {
    ObjectStoreMetadataDto dto = newMetaDto("invalidGroup");
    Assertions.assertThrows(AccessDeniedException.class, () -> repo.create(dto));
  }

  @Test
  @WithMockKeycloakUser(groupRole = {"CNC:STAFF"})
  void create_Authorized_RecordCreated() {
    ObjectStoreMetadataDto dto = newMetaDto(GROUP_1);
    UUID uuid = repo.create(dto).getUuid();
    Assertions.assertNotNull(uuid);
  }

  @Test
  @WithMockKeycloakUser(groupRole = {"Invalid:STAFF"})
  public void save_UnAuthorizedGroup_ThrowsAccessDeniedException() {
    ObjectStoreMetadataDto toUpdate = repo.findOne(
      persisted.getUuid(), new QuerySpec(ObjectStoreMetadataDto.class));
    toUpdate.setAcCaption("new");
    Assertions.assertThrows(AccessDeniedException.class, () -> repo.save(toUpdate));
  }

  @Test
  @WithMockKeycloakUser(groupRole = {"CNC:STAFF"})
  public void save_AuthorizedGroup_UpdatesObject() {
    String expected = "new value";
    ObjectStoreMetadataDto toUpdate = repo.findOne(
      persisted.getUuid(), new QuerySpec(ObjectStoreMetadataDto.class));
    toUpdate.setAcCaption(expected);
    ObjectStoreMetadataDto result = repo.save(toUpdate);
    Assertions.assertEquals(expected, result.getAcCaption());
  }

  @Test
  @WithMockKeycloakUser(groupRole = {"CNC:STAFF"})
  public void delete_AuthorizedGroup_UpdatesObject() {
    ObjectStoreMetadataDto dto = repo.create(newMetaDto(GROUP_1));
    repo.delete(dto.getUuid());
    Assertions.assertThrows(
      GoneException.class,
      () -> repo.findOne(dto.getUuid(), new QuerySpec(ObjectStoreMetadataDto.class)));
  }

  @Test
  @WithMockKeycloakUser(groupRole = {"Invalid:STAFF"})
  public void delete_UnAuthorizedGroup_ThrowsAccessDeniedException() {
    ObjectStoreMetadata group1 = persistMeta("Group1");
    Assertions.assertThrows(AccessDeniedException.class, () -> repo.delete(group1.getUuid()));
  }

  private ObjectStoreMetadataDto newMetaDto(String group) {
    ObjectStoreMetadataDto meta = new ObjectStoreMetadataDto();
    meta.setFileIdentifier(testObjectUpload.getFileIdentifier());
    meta.setDcType(DcType.IMAGE);
    meta.setBucket(group);
    meta.setXmpRightsUsageTerms(MinioTestConfiguration.TEST_USAGE_TERMS);
    meta.setCreatedBy(RandomStringUtils.random(4));
    return meta;
  }

  private ObjectStoreMetadata persistMeta(String group) {
    ObjectUpload objectUpload = ObjectUploadFactory.newObjectUpload().build();
    service.save(objectUpload);
    ObjectStoreMetadata meta = ObjectStoreMetadataFactory.newObjectStoreMetadata().build();
    meta.setBucket(group);
    meta.setFileIdentifier(objectUpload.getFileIdentifier());
    service.save(meta);
    return meta;
  }
}
