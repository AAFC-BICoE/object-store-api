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
    fetchAllMeta().forEach(objectStoreMetaDataService::delete);
  }

  @Override
  public void testSave() {
    assertNull(objectStoreMetaUnderTest.getId());
    objectStoreMetaDataService.create(objectStoreMetaUnderTest);
    assertNotNull(objectStoreMetaUnderTest.getId());
  }

  @Override
  public void testFind() {
    ObjectStoreMetadata fetchedObjectStoreMeta = objectStoreMetaDataService.findOne(
      objectStoreMetaUnderTest.getUuid(), ObjectStoreMetadata.class);

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
    UUID uuid = objectStoreMetaUnderTest.getUuid();
    objectStoreMetaDataService.delete(objectStoreMetaUnderTest);
    assertNull(objectStoreMetaDataService.findOne(
      uuid, ObjectStoreMetadata.class));
  }

  @Test
  public void testRelationships() {
    ManagedAttribute ma = ManagedAttributeFactory.newManagedAttribute().build();
    managedAttributeService.create(ma);

    ObjectSubtype ost = ObjectSubtypeFactory.newObjectSubtype().build();
    objectSubTypeService.create(ost);

    ObjectStoreMetadata osm = ObjectStoreMetadataFactory
        .newObjectStoreMetadata()
        .acDigitizationDate(TEST_OFFSET_DT)
        .acSubType(ost)
        .build();

    // Use "true" here to detach the Metadata,
    // which will make sure the getAcSubTypeId read-only field is populated when the Metadata is restored. 
    objectStoreMetaDataService.create(osm);

    ObjectStoreMetadata restoredOsm = objectStoreMetaDataService.findOne(osm.getUuid(), ObjectStoreMetadata.class);
    assertNotNull(restoredOsm.getId());

    OffsetDateTime initialTimestamp = restoredOsm.getXmpMetadataDate();

    // link the 2 entities
    MetadataManagedAttribute mma = MetadataManagedAttributeFactory.newMetadataManagedAttribute()
    .objectStoreMetadata(restoredOsm)
    .managedAttribute(ma)
    .assignedValue("test value")
    .build();

    metaManagedAttributeService.create(mma);

    OffsetDateTime newTimestamp = restoredOsm.getXmpMetadataDate();

    // Adding a MetadataManagedAttribute should update the parent ObjectStoreMetadata:
    assertNotEquals(newTimestamp, initialTimestamp);

    MetadataManagedAttribute restoredMma = metaManagedAttributeService.findOne(
      mma.getUuid(), MetadataManagedAttribute.class);
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
    return objectStoreMetaDataService.findAll(ObjectStoreMetadata.class,
      (criteriaBuilder, objectStoreMetadataRoot) -> new Predicate[0],
      null, 0, 100);
  }

}
