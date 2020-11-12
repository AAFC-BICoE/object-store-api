package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.dina.testsupport.security.WithMockKeycloakUser;
import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.MinioTestConfiguration;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.respository.ObjectStoreResourceRepository;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import io.crnk.core.queryspec.QuerySpec;
import org.javers.core.Javers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import javax.inject.Inject;

@SpringBootTest(properties = "keycloak.enabled=true")
@Import(MinioTestConfiguration.class)
public class MetaDataPermissionIT extends BaseIntegrationTest {

  @Inject
  private ObjectStoreResourceRepository repo;
  @Inject
  private Javers javers;

  private ObjectStoreMetadata metadata;
  private ObjectUpload objectUpload;
  private ObjectStoreMetadata thumbMeta;

  @BeforeEach
  void setUp() {
    thumbMeta = ObjectStoreMetadataFactory.newObjectStoreMetadata().build();
    service.save(thumbMeta);

    objectUpload = MinioTestConfiguration.buildTestObjectUpload();
    objectUpload.setThumbnailIdentifier(thumbMeta.getFileIdentifier());
    service.save(objectUpload);

    metadata = ObjectStoreMetadataFactory.newObjectStoreMetadata().build();
    metadata.setFileIdentifier(objectUpload.getFileIdentifier());
    service.save(metadata);
    //Create audit snapshot
    repo.save(repo.findOne(metadata.getUuid(), new QuerySpec(ObjectStoreMetadataDto.class)));
  }

  @WithMockKeycloakUser(groupRole = {"group 1:COLLECTION_MANAGER"})
  @Test
  void delete_OnSecondDeleteWhenCollectionManager_HardDeletes() {
    Assertions.assertNotNull(service.find(ObjectStoreMetadata.class, metadata.getId()));
    Assertions.assertNotNull(service.find(ObjectUpload.class, objectUpload.getId()));
    //first delete
    repo.delete(metadata.getUuid());
    Assertions.assertNotNull(service.find(ObjectStoreMetadata.class, metadata.getId()));
    Assertions.assertNotNull(service.find(ObjectUpload.class, objectUpload.getId()));
    Assertions.assertNotNull(service.find(ObjectStoreMetadata.class, thumbMeta.getId()));
    Assertions.assertTrue(
      javers.getLatestSnapshot(metadata.getUuid(), ObjectStoreMetadataDto.class).isPresent(),
      "snap shot should still exist for this metadata");
    //second delete
    repo.delete(metadata.getUuid());
    Assertions.assertNull(service.find(ObjectStoreMetadata.class, metadata.getId()));
    Assertions.assertNull(service.find(ObjectUpload.class, objectUpload.getId()));
    Assertions.assertNull(service.find(ObjectStoreMetadata.class, thumbMeta.getId()));
    Assertions.assertTrue(
      javers.getLatestSnapshot(metadata.getUuid(), ObjectStoreMetadataDto.class).isEmpty(),
      "snapshot should be removed for this metadata");
  }

  @WithMockKeycloakUser(groupRole = {"group 1:STAFF"})
  @Test
  void delete_OnSecondDeleteWhenStaff_SoftDeletes() {
    Assertions.assertNotNull(service.find(ObjectStoreMetadata.class, metadata.getId()));
    repo.delete(metadata.getUuid());
    repo.delete(metadata.getUuid());
    ObjectStoreMetadata actual = service.find(ObjectStoreMetadata.class, metadata.getId());
    Assertions.assertNotNull(actual);
    Assertions.assertNotNull(actual.getDeletedDate());
    Assertions.assertNotNull(service.find(ObjectUpload.class, objectUpload.getId()));
    Assertions.assertTrue(
      javers.getLatestSnapshot(metadata.getUuid(), ObjectStoreMetadataDto.class).isPresent(),
      "snap shot should still exist for this metadata");
  }

}
