package ca.gc.aafc.objectstore.api.crud;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.validation.ValidationException;

import com.google.common.collect.ImmutableMap;

import org.junit.jupiter.api.Test;

import ca.gc.aafc.objectstore.api.entities.ObjectStoreManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreManagedAttributeFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;

public class ObjectStoreManagedAttributeCRUDIT extends BaseEntityCRUDIT {
     
  private ObjectStoreManagedAttribute managedAttributeUnderTest = ObjectStoreManagedAttributeFactory.newManagedAttribute()
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
    ObjectStoreManagedAttribute blankDescription = ObjectStoreManagedAttributeFactory.newManagedAttribute()
      .acceptedValues(new String[] { "a", "b" })
      .description(ImmutableMap.of("en", ""))
      .build();

    assertThrows(
      ValidationException.class,
      () -> managedAttributeService.create(blankDescription));
  }

  @Test
  public void testSave_whenDescriptionIsNull_throwValidationException() {
    ObjectStoreManagedAttribute nullDescription = ObjectStoreManagedAttributeFactory.newManagedAttribute()
      .acceptedValues(new String[] { "a", "b" })
      .description(null)
      .build();

    assertThrows(
      ValidationException.class,
      () -> managedAttributeService.create(nullDescription));
  }

  @Override
  public void testFind() {
    ObjectStoreManagedAttribute fetchedObjectStoreMeta = managedAttributeService.findOne(
      managedAttributeUnderTest.getUuid(), ObjectStoreManagedAttribute.class);
    assertEquals(managedAttributeUnderTest.getId(), fetchedObjectStoreMeta.getId());

    assertArrayEquals(new String[] { "a", "b" }, managedAttributeUnderTest.getAcceptedValues());

    assertEquals("attrFr", managedAttributeUnderTest.getDescription().get("fr"));
    assertEquals(managedAttributeUnderTest.getCreatedBy(), fetchedObjectStoreMeta.getCreatedBy());
    assertNotNull(fetchedObjectStoreMeta.getCreatedOn());
  }

  @Test
  public void testRemove() {
    UUID uuid = managedAttributeUnderTest.getUuid();
    managedAttributeService.delete(managedAttributeUnderTest);
    assertNull(managedAttributeService.findOne(
      uuid, ObjectStoreManagedAttribute.class));
  }

  @Test
  public void testRemove_WhenKeyInUseByMetadata_DeniesDelete() {
    ObjectStoreManagedAttribute managedAttribute = ObjectStoreManagedAttributeFactory.newManagedAttribute()
      .acceptedValues(new String[] { "key_a", "value_a" })
      .description(ImmutableMap.of("en", "attrEn", "fr", "attrFr"))
      .createdBy("createdBy")
      .build();

    managedAttributeService.create(managedAttribute);
    
    ObjectStoreMetadata objectStoreMetadata = ObjectStoreMetadataFactory.newObjectStoreMetadata()
    .managedAttributeValues(new HashMap<> (Map.of(managedAttribute.getKey(), "value_a")))
    .build();
    
    ObjectUpload upload = ObjectUploadFactory.newObjectUpload().fileIdentifier(objectStoreMetadata.getFileIdentifier()).build();

    objectUploadService.create(upload);
    objectStoreMetaDataService.create(objectStoreMetadata);

    assertNotNull(objectStoreMetaDataService.findOne(
      objectStoreMetadata.getUuid(), ObjectStoreMetadata.class));

    assertThrows(
      IllegalStateException.class, () -> managedAttributeService.delete(managedAttribute));
  }
}
