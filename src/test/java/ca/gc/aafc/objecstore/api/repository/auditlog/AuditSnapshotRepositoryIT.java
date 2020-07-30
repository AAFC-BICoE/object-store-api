package ca.gc.aafc.objecstore.api.repository.auditlog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.gc.aafc.objectstore.api.dto.AuditSnapshotDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.repository.BaseRepositoryTest;
import ca.gc.aafc.objectstore.api.respository.auditlog.AuditSnapshotRepository;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.resource.meta.PagedMetaInformation;

public class AuditSnapshotRepositoryIT extends BaseRepositoryTest {

  @Inject
  private AuditSnapshotRepository snapshotRepo;

  @Inject
  private EntityManager entityManager;

  private ObjectStoreMetadata metadata1;
  private ObjectStoreMetadata metadata2;

  @BeforeEach
  public void setup() {
    // Create metadata1
    metadata1 = ObjectStoreMetadataFactory.newObjectStoreMetadata().build();
    entityManager.persist(metadata1);
    entityManager.flush();

    // Update metadata1
    metadata1.setAcTags(new String[] { "tag1", "tag2" });
    entityManager.flush();

    // Delete metadata1
    entityManager.remove(metadata1);
    entityManager.flush();

    // Create metadata2
    metadata2 = ObjectStoreMetadataFactory.newObjectStoreMetadata().build();
    entityManager.persist(metadata2);
    entityManager.flush();

    // Update metadata2
    metadata2.setAcTags(new String[] { "tag3", "tag4" });
    entityManager.flush();
  }

  @Test
  public void findAll_whenNoFilter_allSnapshotsReturned() {
    QuerySpec qs = new QuerySpec(AuditSnapshotDto.class);
    ResourceList<AuditSnapshotDto> snapshots = snapshotRepo.findAll(qs);
    assertNotEquals(0, snapshots.size());
    assertNotEquals(0, ((PagedMetaInformation) snapshots.getMeta()).getTotalResourceCount());
  }
  
  @Test
  public void findAll_whenFilteredByInstance_snapshotsFiltered() {
    QuerySpec qs = new QuerySpec(AuditSnapshotDto.class);
    qs.addFilter(filter("instanceId", "metadata/" + metadata1.getUuid()));
    ResourceList<AuditSnapshotDto> snapshots = snapshotRepo.findAll(qs);
    assertEquals(3, snapshots.size());
    assertEquals(3, ((PagedMetaInformation) snapshots.getMeta()).getTotalResourceCount());
  }

  @Test
  public void findAll_whenFilteredByAuthor_snapshotsFiltered() {
    QuerySpec qs1 = new QuerySpec(AuditSnapshotDto.class);
    qs1.addFilter(filter("author", "anonymous"));
    ResourceList<AuditSnapshotDto> snapshots1 = snapshotRepo.findAll(qs1);
    assertNotEquals(0, snapshots1.size());
    assertNotEquals(0, ((PagedMetaInformation) snapshots1.getMeta()).getTotalResourceCount());
    
    QuerySpec qs2 = new QuerySpec(AuditSnapshotDto.class);
    qs2.addFilter(filter("author", "other-user"));
    ResourceList<AuditSnapshotDto> snapshots2 = snapshotRepo.findAll(qs2);
    assertEquals(0, snapshots2.size());
    assertEquals(0, ((PagedMetaInformation) snapshots2.getMeta()).getTotalResourceCount());
  }

  private FilterSpec filter(String attribute, String value) {
    return new FilterSpec(PathSpec.of(attribute), FilterOperator.EQ, value);
  }

}
