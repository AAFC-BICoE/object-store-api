package ca.gc.aafc.objectstore.api.auditing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.javers.core.Javers;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.javers.repository.jql.JqlQuery;
import org.javers.repository.jql.QueryBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
public class AuditListenerIT {

  @Inject
  private Javers javers;

  @Inject
  private BaseDAO baseDao;

  @Inject
  private EntityManager entityManager;

  @Test
  public void addMetadata_whenAdded_snapshotCreated() {
    ObjectStoreMetadata metadata = addMetadata();
    CdoSnapshot latest = javers.getLatestSnapshot(metadata.getUuid().toString(), ObjectStoreMetadataDto.class).get();
    assertEquals("INITIAL", latest.getType().toString()); // INITIAL snapshot created.
    assertEquals(1, snapshotCount(metadata));
  }
  
  @Test
  public void updateMetadata_whenUpdated_snapshotCreated() {
    ObjectStoreMetadata metadata = addMetadata();
    metadata.setAcTags(new String[] { "t1", "t2" });
    entityManager.flush();
    CdoSnapshot latest = javers.getLatestSnapshot(metadata.getUuid().toString(), ObjectStoreMetadataDto.class).get();
    assertEquals("UPDATE", latest.getType().toString()); // UPDATE snapshot created.
    assertEquals(Arrays.asList("acTags"), latest.getChanged()); // UPDATE snapshot created.
    assertEquals(2, snapshotCount(metadata));
  }
  
  @Test
  public void softDeleteMetadata_whenSoftDeleted_snapshotCreated() {
    ObjectStoreMetadata metadata = addMetadata();
    metadata.setDeletedDate(OffsetDateTime.now());
    entityManager.flush();
    CdoSnapshot latest = javers.getLatestSnapshot(metadata.getUuid().toString(), ObjectStoreMetadataDto.class).get();
    assertEquals("TERMINAL", latest.getType().toString()); // TERMINAL snapshot created.
    assertEquals(2, snapshotCount(metadata));
  }
  
  @Test
  public void deleteMetadata_whenDeleted_snapshotCreated() {
    ObjectStoreMetadata metadata = addMetadata();
    baseDao.delete(metadata);
    entityManager.flush();
    CdoSnapshot latest = javers.getLatestSnapshot(metadata.getUuid().toString(), ObjectStoreMetadataDto.class).get();
    assertEquals("TERMINAL", latest.getType().toString()); // TERMINAL snapshot created.
    assertEquals(2, snapshotCount(metadata));
  }

  private ObjectStoreMetadata addMetadata() {
    ObjectStoreMetadata metadata = ObjectStoreMetadataFactory.newObjectStoreMetadata()
        .managedAttribute(new ArrayList<>()).build();
    entityManager.persist(metadata);
    entityManager.flush();
    return metadata;
  }

  private int snapshotCount(ObjectStoreMetadata metadata) {
    JqlQuery query = QueryBuilder.byInstanceId(metadata.getUuid().toString(), "metadata").build();
    List<CdoSnapshot> snapshots = javers.findSnapshots(query);
    return snapshots.size();
  }

}
