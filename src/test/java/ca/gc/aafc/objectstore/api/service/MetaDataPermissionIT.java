package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.dina.testsupport.security.WithMockKeycloakUser;
import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.respository.ObjectStoreResourceRepository;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.inject.Inject;

@SpringBootTest(properties = "keycloak.enabled=true")
public class MetaDataPermissionIT extends BaseIntegrationTest {

  @Inject
  private ObjectStoreResourceRepository repo;

  private ObjectStoreMetadata metadata;

  @BeforeEach
  void setUp() {
    metadata = ObjectStoreMetadataFactory.newObjectStoreMetadata().build();
    service.save(metadata);
  }

  @WithMockKeycloakUser(groupRole = {"group 1:COLLECTION_MANAGER"})
  @Test
  void delete_WhenCollectionManager_HardDeletes() {
    Assertions.assertNotNull(service.find(ObjectStoreMetadata.class, metadata.getId()));
    repo.delete(metadata.getUuid());
    Assertions.assertNull(service.find(ObjectStoreMetadata.class, metadata.getId()));
  }

  @WithMockKeycloakUser(groupRole = {"group 1:STAFF"})
  @Test
  void delete_WhenNOTCollectionManager_SoftDeletes() {
    Assertions.assertNotNull(service.find(ObjectStoreMetadata.class, metadata.getId()));
    repo.delete(metadata.getUuid());
    ObjectStoreMetadata actual = service.find(ObjectStoreMetadata.class, metadata.getId());
    Assertions.assertNotNull(actual);
    Assertions.assertNotNull(actual.getDeletedDate());
  }
}
