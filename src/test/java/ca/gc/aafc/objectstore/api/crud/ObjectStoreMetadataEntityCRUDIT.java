package ca.gc.aafc.objectstore.api.crud;

import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import ca.gc.aafc.objectstore.api.testsupport.factories.ManagedAttributeFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.MetadataManagedAttributeFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectSubtypeFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.persistence.criteria.Predicate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectStoreMetadataEntityCRUDIT extends BaseEntityCRUDIT {

  @Inject
  private DinaService<ObjectStoreMetadata> metaService;

  private static final ZoneId MTL_TZ = ZoneId.of("America/Montreal");
  private final ZonedDateTime TEST_ZONED_DT = ZonedDateTime.of(2019, 1, 2, 3, 4, 5, 0, MTL_TZ);
  private final OffsetDateTime TEST_OFFSET_DT = TEST_ZONED_DT.toOffsetDateTime();

  private final ObjectStoreMetadata objectStoreMetaUnderTest = ObjectStoreMetadataFactory
      .newObjectStoreMetadata()
      .acMetadataCreator(UUID.randomUUID())
      .dcCreator(UUID.randomUUID())
      .acDigitizationDate(TEST_OFFSET_DT)
      .build();

  @BeforeEach
  void setUp() {
    // Clean all database entries
    fetchAllMeta().forEach(metaService::delete);
  }

  @Override
  public void testSave() {
    assertNull(objectStoreMetaUnderTest.getId());
    service.save(objectStoreMetaUnderTest);
    assertNotNull(objectStoreMetaUnderTest.getId());
  }

  @Override
  public void testFind() {
    ObjectStoreMetadata fetchedObjectStoreMeta = service.find(ObjectStoreMetadata.class,
        objectStoreMetaUnderTest.getId());

    assertEquals(objectStoreMetaUnderTest.getId(), fetchedObjectStoreMeta.getId());
    assertEquals(objectStoreMetaUnderTest.getDcCreator(), fetchedObjectStoreMeta.getDcCreator());
    assertEquals(objectStoreMetaUnderTest.getCreatedBy(), fetchedObjectStoreMeta.getCreatedBy());
    assertEquals(objectStoreMetaUnderTest.getId(), fetchedObjectStoreMeta.getId());
    assertEquals(
      objectStoreMetaUnderTest.getAcMetadataCreator(),
      fetchedObjectStoreMeta.getAcMetadataCreator());

    // the returned acDigitizationDate will use the timezone of the server
    assertEquals(objectStoreMetaUnderTest.getAcDigitizationDate(),
        fetchedObjectStoreMeta.getAcDigitizationDate()
        .atZoneSameInstant(MTL_TZ)
        .toOffsetDateTime());

    //should be auto-generated
    assertNotNull(fetchedObjectStoreMeta.getCreatedOn());
    assertNotNull(fetchedObjectStoreMeta.getXmpMetadataDate());
  }

  @Override
  public void testRemove() {
    Integer id = objectStoreMetaUnderTest.getId();
    service.deleteById(ObjectStoreMetadata.class, id);
    assertNull(service.find(ObjectStoreMetadata.class, id));
  }

  @Test
  void test_AddingDerivatives_RelationShipEstablished() {
    ObjectStoreMetadata parent = ObjectStoreMetadataFactory.newObjectStoreMetadata().build();
    ObjectStoreMetadata child = ObjectStoreMetadataFactory.newObjectStoreMetadata().build();

    parent.addDerivative(child);
    metaService.create(parent);

    ObjectStoreMetadata resultChild = service.find(ObjectStoreMetadata.class, child.getId());
    ObjectStoreMetadata resultParent = service.find(ObjectStoreMetadata.class, parent.getId());

    Assertions.assertEquals(resultChild.getAcDerivedFrom().getId(), resultParent.getId());

    List<ObjectStoreMetadata> resultDerivatives = resultParent.getDerivatives();
    Assertions.assertEquals(1, resultDerivatives.size());
    Assertions.assertEquals(resultChild.getId(), resultDerivatives.get(0).getId());
  }

  @Test
  void test_RemovingDerivatives_RelationRemoved() {
    ObjectStoreMetadata parent = ObjectStoreMetadataFactory.newObjectStoreMetadata().build();
    ObjectStoreMetadata child = ObjectStoreMetadataFactory.newObjectStoreMetadata().build();
    parent.addDerivative(child);
    metaService.create(parent);

    ObjectStoreMetadata update = service.find(ObjectStoreMetadata.class, parent.getId());
    update.removeDerivative(service.find(ObjectStoreMetadata.class, child.getId()));
    metaService.update(parent);

    ObjectStoreMetadata resultChild = service.find(ObjectStoreMetadata.class, child.getId());
    ObjectStoreMetadata resultParent = service.find(ObjectStoreMetadata.class, parent.getId());

    Assertions.assertNull(resultChild.getAcDerivedFrom());
    Assertions.assertEquals(0, resultParent.getDerivatives().size());
  }

  @Test
  void test_DeleteParent_ChildrenNotDeleted() {
    ObjectStoreMetadata parent = ObjectStoreMetadataFactory.newObjectStoreMetadata().build();
    ObjectStoreMetadata child = ObjectStoreMetadataFactory.newObjectStoreMetadata().build();

    parent.addDerivative(child);
    metaService.create(parent);

    // Needed to force a flush
    fetchAllMeta();
    Assertions.assertNotNull(metaService.findOne(child.getUuid(), ObjectStoreMetadata.class));
    Assertions.assertNotNull(metaService.findOne(parent.getUuid(), ObjectStoreMetadata.class));

    metaService.delete(parent);

    // Needed to force a flush
    fetchAllMeta();
    //Parent is soft deleted
    Assertions.assertNotNull(
      metaService.findOne(parent.getUuid(), ObjectStoreMetadata.class).getDeletedDate());

    ObjectStoreMetadata resultChild = metaService.findOne(child.getUuid(), ObjectStoreMetadata.class);
    Assertions.assertNotNull(resultChild);
    Assertions.assertNull(resultChild.getAcDerivedFrom());
  }

  @Test
  public void testRelationships() {
    ManagedAttribute ma = ManagedAttributeFactory.newManagedAttribute().build();
    service.save(ma, false);

    ObjectSubtype ost = ObjectSubtypeFactory.newObjectSubtype().build();
    service.save(ost, false);

    ObjectStoreMetadata osm = ObjectStoreMetadataFactory
        .newObjectStoreMetadata()
        .acDigitizationDate(TEST_OFFSET_DT)
        .acSubType(ost)
        .build();

    // Use "true" here to detach the Metadata,
    // which will make sure the getAcSubTypeId read-only field is populated when the Metadata is restored. 
    service.save(osm, true);

    ObjectStoreMetadata restoredOsm = service.find(ObjectStoreMetadata.class, osm.getId());
    assertNotNull(restoredOsm.getId());

    OffsetDateTime initialTimestamp = restoredOsm.getXmpMetadataDate();

    // link the 2 entities
    MetadataManagedAttribute mma = MetadataManagedAttributeFactory.newMetadataManagedAttribute()
    .objectStoreMetadata(restoredOsm)
    .managedAttribute(ma)
    .assignedValue("test value")
    .build();

    service.save(mma);

    OffsetDateTime newTimestamp = restoredOsm.getXmpMetadataDate();

    // Adding a MetadataManagedAttribute should update the parent ObjectStoreMetadata:
    assertNotEquals(newTimestamp, initialTimestamp);

    MetadataManagedAttribute restoredMma = service.find(MetadataManagedAttribute.class, mma.getId());
    assertEquals(restoredOsm.getId(), restoredMma.getObjectStoreMetadata().getId());

    // Test read-only getAcSubTypeId Formula field:
    assertEquals(ost.getId(), restoredOsm.getAcSubTypeId());
    assertEquals(ost.getId(), restoredOsm.getAcSubType().getId());
  }

  /**
   * Helper method to return a list of all metadata from the database. Note this will flush Hibernate changes.
   *
   * @return list of all metadata from the database.
   */
  private List<ObjectStoreMetadata> fetchAllMeta() {
    return metaService.findAll(ObjectStoreMetadata.class,
      (criteriaBuilder, objectStoreMetadataRoot) -> new Predicate[0],
      null, 0, 100);
  }

}
