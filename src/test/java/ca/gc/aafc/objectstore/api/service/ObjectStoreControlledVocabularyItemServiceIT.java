package ca.gc.aafc.objectstore.api.service;

import org.junit.jupiter.api.Test;

import ca.gc.aafc.dina.i18n.MultilingualDescription;
import ca.gc.aafc.dina.vocabulary.TypedVocabularyElement;
import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreControlledVocabularyItem;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.testsupport.factories.MultilingualDescriptionFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreControlledVocabularyItemTestFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import jakarta.validation.ValidationException;

public class ObjectStoreControlledVocabularyItemServiceIT extends BaseIntegrationTest {

  private ObjectStoreControlledVocabularyItem buildObjectStoreManagedAttribute() {
    return ObjectStoreControlledVocabularyItemTestFactory.newObjectStoreControlledVocabularyItem()
      .acceptedValues(new String[] { "a", "b" })
      .multilingualDescription(MultilingualDescriptionFactory.newMultilingualDescription().build())
      .createdBy("createdBy")
      .controlledVocabulary(getManagedAttributeControlledVocabularyRef())
      .build();
  }
      
  @Test
  public void testSave() {
    ObjectStoreControlledVocabularyItem managedAttributeUnderTest = buildObjectStoreManagedAttribute();
    controlledVocabularyItemService.create(managedAttributeUnderTest);
    assertNotNull(managedAttributeUnderTest.getId());
  }

  @Test
  public void testSaveAllTypes() {
    for(var type : TypedVocabularyElement.VocabularyElementType.values()) {
      controlledVocabularyItemService.create(
        ObjectStoreControlledVocabularyItemTestFactory.newObjectStoreControlledVocabularyItem()
        .controlledVocabulary(getManagedAttributeControlledVocabularyRef())
        .vocabularyElementType(type)
        .build());
    }
  }

  @Test
  public void testSave_whenDescriptionIsBlank_throwValidationException() {
    ObjectStoreControlledVocabularyItem blankDescription = ObjectStoreControlledVocabularyItemTestFactory.newObjectStoreControlledVocabularyItem()
      .acceptedValues(new String[] { "a", "b" })
      .controlledVocabulary(getManagedAttributeControlledVocabularyRef())
      .multilingualDescription(MultilingualDescription.builder()
          .descriptions(List.of(MultilingualDescription.MultilingualPair.of("en", "")))
          .build())
      .build();

    assertThrows(
      ValidationException.class,
      () -> controlledVocabularyItemService.create(blankDescription));
  }

  @Test
  public void testSave_whenDescriptionsIsNull_throwValidationException() {
    ObjectStoreControlledVocabularyItem nullDescription = ObjectStoreControlledVocabularyItemTestFactory.newObjectStoreControlledVocabularyItem()
      .acceptedValues(new String[] { "a", "b" })
      .controlledVocabulary(getManagedAttributeControlledVocabularyRef())
      .multilingualDescription(MultilingualDescription.builder()
          .descriptions(null)
          .build())
      .build();

    assertThrows(
      ValidationException.class,
      () -> controlledVocabularyItemService.create(nullDescription));
  }

  @Test
  public void testFind() {
    ObjectStoreControlledVocabularyItem managedAttributeUnderTest = buildObjectStoreManagedAttribute();
    controlledVocabularyItemService.create(managedAttributeUnderTest);

    ObjectStoreControlledVocabularyItem fetchedObjectStoreMeta = controlledVocabularyItemService.findOne(
      managedAttributeUnderTest.getUuid(), ObjectStoreControlledVocabularyItem.class);
    assertEquals(managedAttributeUnderTest.getId(), fetchedObjectStoreMeta.getId());

    assertArrayEquals(new String[] { "a", "b" }, managedAttributeUnderTest.getAcceptedValues());

    assertEquals("attrFr", managedAttributeUnderTest.getMultilingualDescription().getDescriptions().stream().filter(p -> p.getLang().equals("fr")).findAny().get().getDesc());
    assertEquals(managedAttributeUnderTest.getCreatedBy(), fetchedObjectStoreMeta.getCreatedBy());
    assertNotNull(fetchedObjectStoreMeta.getCreatedOn());
  }

  @Test
  public void testRemove() {
    ObjectStoreControlledVocabularyItem managedAttributeUnderTest = buildObjectStoreManagedAttribute();
    controlledVocabularyItemService.create(managedAttributeUnderTest);

    UUID uuid = managedAttributeUnderTest.getUuid();
    controlledVocabularyItemService.delete(managedAttributeUnderTest);
    assertNull(controlledVocabularyItemService.findOne(
      uuid, ObjectStoreControlledVocabularyItem.class));
  }

  @Test
  public void testRemove_WhenKeyInUseByMetadata_DeniesDelete() {
    ObjectStoreControlledVocabularyItem managedAttribute = ObjectStoreControlledVocabularyItemTestFactory.newObjectStoreControlledVocabularyItem()
      .acceptedValues(new String[] { "key_a", "value_a" })
      .multilingualDescription(MultilingualDescriptionFactory.newMultilingualDescription().build())
      .createdBy("createdBy")
      .controlledVocabulary(getManagedAttributeControlledVocabularyRef())
      .build();

    controlledVocabularyItemService.create(managedAttribute);
    
    ObjectStoreMetadata objectStoreMetadata = ObjectStoreMetadataFactory.newObjectStoreMetadata()
    .managedAttributes(new HashMap<> (Map.of(managedAttribute.getKey(), "value_a")))
    .build();
    
    ObjectUpload upload = ObjectUploadFactory.newObjectUpload().fileIdentifier(objectStoreMetadata.getFileIdentifier()).build();

    objectUploadService.create(upload);
    objectStoreMetaDataService.create(objectStoreMetadata);

    assertNotNull(objectStoreMetaDataService.findOne(
      objectStoreMetadata.getUuid(), ObjectStoreMetadata.class));

    assertThrows(
      IllegalStateException.class, () -> controlledVocabularyItemService.delete(managedAttribute));
  }
}
