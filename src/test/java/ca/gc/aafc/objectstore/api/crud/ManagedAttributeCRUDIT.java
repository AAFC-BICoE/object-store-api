package ca.gc.aafc.objectstore.api.crud;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.google.common.collect.ImmutableMap;

import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.testsupport.factories.ManagedAttributeFactory;

public class ManagedAttributeCRUDIT extends BaseEntityCRUDIT {
     
  private ManagedAttribute managedAttributeUnderTest = ManagedAttributeFactory.newManagedAttribute()
      .acceptedValues(new String[] { "a", "b" })
      .description(ImmutableMap.of("en", "attrEn", "fr", "attrFr"))
      .build();
  

      
  @Override
  public void testSave() {
    assertNull(managedAttributeUnderTest.getId());
    service.save(managedAttributeUnderTest);
    assertNotNull(managedAttributeUnderTest.getId());
  }

  @Override
  public void testFind() {
    ManagedAttribute fetchedObjectStoreMeta = service.find(ManagedAttribute.class,
        managedAttributeUnderTest.getId());
    assertEquals(managedAttributeUnderTest.getId(), fetchedObjectStoreMeta.getId());

    assertArrayEquals(new String[] { "a", "b" }, managedAttributeUnderTest.getAcceptedValues());

    assertEquals("attrFr", managedAttributeUnderTest.getDescription().get("fr"));
    assertNotNull(fetchedObjectStoreMeta.getCreatedDate());
  }

  @Override
  public void testRemove() {
    Integer id = managedAttributeUnderTest.getId();
    service.deleteById(ManagedAttribute.class, id);
    assertNull(service.find(ManagedAttribute.class, id));
  }
}
