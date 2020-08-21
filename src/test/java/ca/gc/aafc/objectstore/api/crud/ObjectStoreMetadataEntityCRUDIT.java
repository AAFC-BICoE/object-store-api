package ca.gc.aafc.objectstore.api.crud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import ca.gc.aafc.objectstore.api.testsupport.factories.ManagedAttributeFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.MetadataManagedAttributeFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectSubtypeFactory;

public class ObjectStoreMetadataEntityCRUDIT extends BaseEntityCRUDIT {

  private static final ZoneId MTL_TZ = ZoneId.of("America/Montreal");
  private final ZonedDateTime TEST_ZONED_DT = ZonedDateTime.of(2019, 1, 2, 3, 4, 5, 0, MTL_TZ);
  private final OffsetDateTime TEST_OFFSET_DT = TEST_ZONED_DT.toOffsetDateTime();

  private ObjectStoreMetadata objectStoreMetaUnderTest = ObjectStoreMetadataFactory
      .newObjectStoreMetadata()
      .acMetadataCreator(UUID.randomUUID())
      .dcCreator(UUID.randomUUID())
      .acDigitizationDate(TEST_OFFSET_DT)
      .build();

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
  public void testRelationships() {
    ManagedAttribute ma = ManagedAttributeFactory.newManagedAttribute().build();
    service.save(ma, false);

    ObjectStoreMetadata derivedFrom = ObjectStoreMetadataFactory.newObjectStoreMetadata()
        .fileIdentifier(UUID.randomUUID())
        .build();
    service.save(derivedFrom);

    ObjectSubtype ost = ObjectSubtypeFactory.newObjectSubtype().build();
    service.save(ost, false);
   
    ObjectStoreMetadata osm = ObjectStoreMetadataFactory
        .newObjectStoreMetadata()
        .acDigitizationDate(TEST_OFFSET_DT)
        .acDerivedFrom(derivedFrom)
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

    assertEquals(derivedFrom.getId(), restoredOsm.getAcDerivedFrom().getId());
  }

}
