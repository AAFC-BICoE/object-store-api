package ca.gc.aafc.objectstore.api.service;

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
  public ObjectUpload TEST_OBJECT_UPLOAD = MinioTestConfiguration.buildTestObjectUpload();
  private ObjectStoreMetadata persisted;

  @BeforeEach
  void setUp() {
    service.save(TEST_OBJECT_UPLOAD);
    setupPersisted();
  }

  private void setupPersisted() {
    ObjectUpload objectUpload = ObjectUploadFactory.newObjectUpload().build();
    service.save(objectUpload);
    persisted = ObjectStoreMetadataFactory.newObjectStoreMetadata().build();
    persisted.setBucket(GROUP_1);
    persisted.setFileIdentifier(objectUpload.getFileIdentifier());
    service.save(persisted);
  }

  @AfterEach
  void tearDown() {
    service.deleteById(ObjectUpload.class, TEST_OBJECT_UPLOAD.getId());
    repo.findAll(new QuerySpec(ObjectStoreMetadataDto.class)).forEach(m -> repo.delete(m.getUuid()));
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

  private ObjectStoreMetadataDto newMetaDto(String group) {
    ObjectStoreMetadataDto meta = new ObjectStoreMetadataDto();
    meta.setFileIdentifier(MinioTestConfiguration.TEST_FILE_IDENTIFIER);
    meta.setDcType(DcType.IMAGE);
    meta.setBucket(group);
    meta.setXmpRightsUsageTerms(MinioTestConfiguration.TEST_USAGE_TERMS);
    meta.setCreatedBy(RandomStringUtils.random(4));
    return meta;
  }

}
