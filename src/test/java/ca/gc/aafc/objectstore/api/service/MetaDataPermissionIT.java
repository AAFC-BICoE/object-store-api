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
  private ObjectStoreMetadata child;

  @BeforeEach
  void setUp() {
    thumbMeta = ObjectStoreMetadataFactory.newObjectStoreMetadata().build();

    objectUpload = MinioTestConfiguration.buildTestObjectUpload();
    objectUpload.setThumbnailIdentifier(thumbMeta.getFileIdentifier());
    service.save(objectUpload);

    metadata = ObjectStoreMetadataFactory.newObjectStoreMetadata().build();
    metadata.setFileIdentifier(objectUpload.getFileIdentifier());
    service.save(metadata);

    thumbMeta.setAcDerivedFrom(fetchMetaById(metadata.getId()));
    service.save(thumbMeta);

    child = ObjectStoreMetadataFactory.newObjectStoreMetadata().build();
    child.setAcDerivedFrom(fetchMetaById(metadata.getId()));
    service.save(child);

    //Create audit snapshot
    repo.save(repo.findOne(metadata.getUuid(), new QuerySpec(ObjectStoreMetadataDto.class)));
  }

  @WithMockKeycloakUser(groupRole = {"group 1:COLLECTION_MANAGER"})
  @Test
  void delete_OnSecondDeleteWhenCollectionManager_HardDeletes() {
    Assertions.assertNotNull(fetchMetaById(metadata.getId()));
    Assertions.assertNotNull(service.find(ObjectUpload.class, objectUpload.getId()));
    //first delete
    repo.delete(metadata.getUuid());
    Assertions.assertNotNull(fetchMetaById(metadata.getId()));
    Assertions.assertNotNull(service.find(ObjectUpload.class, objectUpload.getId()));
    Assertions.assertNotNull(fetchMetaById(thumbMeta.getId()));
    Assertions.assertNotNull(fetchMetaById(child.getId()).getAcDerivedFrom());
    Assertions.assertTrue(
      javers.getLatestSnapshot(metadata.getUuid(), ObjectStoreMetadataDto.class).isPresent(),
      "snap shot should still exist for this metadata");
    //second delete
    repo.delete(metadata.getUuid());
    Assertions.assertNull(fetchMetaById(metadata.getId()));
    Assertions.assertNull(service.find(ObjectUpload.class, objectUpload.getId()));
    Assertions.assertNull(fetchMetaById(thumbMeta.getId()));
    Assertions.assertNull(fetchMetaById(child.getId()).getAcDerivedFrom());
    Assertions.assertTrue(
      javers.getLatestSnapshot(metadata.getUuid(), ObjectStoreMetadataDto.class).isEmpty(),
      "snapshot should be removed for this metadata");
  }

  @WithMockKeycloakUser(groupRole = {"group 1:STAFF"})
  @Test
  void delete_OnSecondDeleteWhenStaff_SoftDeletes() {
    Assertions.assertNotNull(fetchMetaById(metadata.getId()));
    repo.delete(metadata.getUuid());
    repo.delete(metadata.getUuid());
    ObjectStoreMetadata actual = fetchMetaById(metadata.getId());
    Assertions.assertNotNull(actual);
    Assertions.assertNotNull(actual.getDeletedDate());
    Assertions.assertNotNull(service.find(ObjectUpload.class, objectUpload.getId()));
    Assertions.assertTrue(
      javers.getLatestSnapshot(metadata.getUuid(), ObjectStoreMetadataDto.class).isPresent(),
      "snap shot should still exist for this metadata");
  }

  private ObjectStoreMetadata fetchMetaById(Integer id) {
    return service.find(ObjectStoreMetadata.class, id);
  }

}
