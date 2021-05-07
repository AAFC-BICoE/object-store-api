package ca.gc.aafc.objectstore.api.crud;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import javax.validation.ValidationException;

import com.google.common.collect.ImmutableMap;

import org.junit.jupiter.api.Test;

import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.testsupport.factories.ManagedAttributeFactory;

public class ManagedAttributeCRUDIT extends BaseEntityCRUDIT {
     
  private ManagedAttribute managedAttributeUnderTest = ManagedAttributeFactory.newManagedAttribute()
      .acceptedValues(new String[] { "a", "b" })
      .description(ImmutableMap.of("en", "attrEn", "fr", "attrFr"))
      .createdBy("createdBy")
      .build();
  

      
  @Override
  public void testSave() {
    assertNull(managedAttributeUnderTest.getId());
    managedAttributeService.create(managedAttributeUnderTest);
    assertNotNull(managedAttributeUnderTest.getId());
  }

  @Test
  public void testSave_whenDescriptionIsBlank_throwValidationException() {
    ManagedAttribute blankDescription = ManagedAttributeFactory.newManagedAttribute()
      .acceptedValues(new String[] { "a", "b" })
      .description(ImmutableMap.of("en", ""))
      .build();

    assertThrows(
      ValidationException.class,
      () -> managedAttributeService.create(blankDescription));
  }

  @Override
  public void testFind() {
    ManagedAttribute fetchedObjectStoreMeta = managedAttributeService.findOne(
      managedAttributeUnderTest.getUuid(), ManagedAttribute.class);
    assertEquals(managedAttributeUnderTest.getId(), fetchedObjectStoreMeta.getId());

    assertArrayEquals(new String[] { "a", "b" }, managedAttributeUnderTest.getAcceptedValues());

    assertEquals("attrFr", managedAttributeUnderTest.getDescription().get("fr"));
    assertEquals(managedAttributeUnderTest.getCreatedBy(), fetchedObjectStoreMeta.getCreatedBy());
    assertNotNull(fetchedObjectStoreMeta.getCreatedOn());
  }

  @Override
  public void testRemove() {
    UUID uuid = managedAttributeUnderTest.getUuid();
    managedAttributeService.delete(managedAttributeUnderTest);
    assertNull(managedAttributeService.findOne(
      uuid, ManagedAttribute.class));
  }
}
