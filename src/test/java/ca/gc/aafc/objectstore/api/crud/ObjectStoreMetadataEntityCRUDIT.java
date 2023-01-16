package ca.gc.aafc.objectstore.api.crud;

import ca.gc.aafc.dina.vocabulary.TypedVocabularyElement;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.testsupport.factories.MultilingualDescriptionFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreManagedAttributeFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectSubtypeFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;

import javax.persistence.criteria.Predicate;
import javax.validation.ValidationException;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectStoreMetadataEntityCRUDIT extends BaseEntityCRUDIT {

  private ObjectUpload objectUpload;

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
    objectUpload = ObjectUploadFactory.newObjectUpload().build();
    objectUploadService.create(objectUpload);
  }

  @Override
  public void testSave() {
    assertNull(objectStoreMetaUnderTest.getId());
    objectStoreMetaUnderTest.setFileIdentifier(objectUpload.getFileIdentifier());
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
    assertEquals(objectStoreMetaUnderTest.getManagedAttributes(),fetchedObjectStoreMeta.getManagedAttributes());
    assertEquals(
      objectStoreMetaUnderTest.getAcMetadataCreator(),
      fetchedObjectStoreMeta.getAcMetadataCreator());
    assertEquals(objectStoreMetaUnderTest.getOrientation(), fetchedObjectStoreMeta.getOrientation());

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
  public void testRemoveDerivativeWhenMetadataIsDeleted() {

    ObjectStoreMetadata osm = ObjectStoreMetadataFactory
      .newObjectStoreMetadata()
      .acDigitizationDate(TEST_OFFSET_DT)
      .fileIdentifier(objectUpload.getFileIdentifier())
      .build();

    objectStoreMetaDataService.create(osm);

    Derivative derivative = newDerivative(osm);
    derivativeService.create(derivative);

    UUID uuid = osm.getUuid();
    UUID derivativeUuid = derivative.getUuid();
    objectStoreMetaDataService.delete(osm);

    assertNull(objectStoreMetaDataService.findOne(
      uuid, ObjectStoreMetadata.class));
    assertNull(derivativeService.findOne(
      derivativeUuid, Derivative.class));
  }

  @Test
  public void testRelationships() {
    ObjectStoreManagedAttribute ma = ObjectStoreManagedAttributeFactory.newManagedAttribute().build();
    managedAttributeService.create(ma);

    ObjectSubtype ost = ObjectSubtypeFactory.newObjectSubtype().build();
    objectSubtypeService.create(ost);

    ObjectStoreMetadata osm = ObjectStoreMetadataFactory
        .newObjectStoreMetadata()
        .acDigitizationDate(TEST_OFFSET_DT)
        .acSubtype(ost)
        .fileIdentifier(objectUpload.getFileIdentifier())
        .build();

    // Use "true" here to detach the Metadata,
    // which will make sure the getAcSubtypeId read-only field is populated when the Metadata is restored. 
    objectStoreMetaDataService.create(osm);

    ObjectStoreMetadata restoredOsm = objectStoreMetaDataService.findOne(osm.getUuid(), ObjectStoreMetadata.class);
    assertNotNull(restoredOsm.getId());

    // Test read-only getAcSubtypeId Formula field:
    assertEquals(ost.getId(), restoredOsm.getAcSubtypeId());
    assertEquals(ost.getId(), restoredOsm.getAcSubtype().getId());
  }

  @Test
  void dcType_WhenCr2_dcTypeSetFromEvaluatedMediaType() {
    ObjectUpload cr2 = ObjectUploadFactory.newObjectUpload()
      .evaluatedMediaType("image/x-cannon-cr2").build();
    objectUploadService.create(cr2);

    objectStoreMetaUnderTest.setFileIdentifier(cr2.getFileIdentifier());
    objectStoreMetaUnderTest.setDcFormat(null);
    objectStoreMetaUnderTest.setDcType(null);
    objectStoreMetaDataService.create(objectStoreMetaUnderTest);

    ObjectStoreMetadata fetchedObjectStoreMeta = objectStoreMetaDataService.findOne(
      objectStoreMetaUnderTest.getUuid(), ObjectStoreMetadata.class);
    Assertions.assertEquals(DcType.IMAGE, fetchedObjectStoreMeta.getDcType());
  }

  @ParameterizedTest
  @ValueSource(strings = {"1.2", "", "  ", "\t", "\n", "a"})
  void testCreate_WhenInvalidIntegerType_ExceptionThrown(String value) {

    ObjectStoreManagedAttribute managedAttribute = ObjectStoreManagedAttributeFactory.newManagedAttribute()
      .acceptedValues(new String[] {})
      .vocabularyElementType(TypedVocabularyElement.VocabularyElementType.INTEGER)
      .build();

    managedAttributeService.create(managedAttribute);
    
    ObjectStoreMetadata objectStoreMetadata = ObjectStoreMetadataFactory.newObjectStoreMetadata()
    .managedAttributeValues(new HashMap<> (Map.of(managedAttribute.getKey(), value)))
    .build();
    
    ObjectUpload upload = ObjectUploadFactory.newObjectUpload().fileIdentifier(objectStoreMetadata.getFileIdentifier()).build();

    objectUploadService.create(upload);
    Assertions.assertThrows(ValidationException.class, 
      () -> objectStoreMetaDataService.create(objectStoreMetadata));
  
  }

  @Test
  void testCreate_assignedValueNotContainedInAcceptedValues_NoExceptionThrown() {
    ObjectStoreManagedAttribute managedAttribute = ObjectStoreManagedAttributeFactory.newManagedAttribute()
      .acceptedValues(new String[] {"val1", "val2"})
      .multilingualDescription(MultilingualDescriptionFactory.newMultilingualDescription().build())
      .createdBy("createdBy")
      .build();

    managedAttributeService.create(managedAttribute);
    
    ObjectStoreMetadata objectStoreMetadata = ObjectStoreMetadataFactory.newObjectStoreMetadata()
    .managedAttributeValues(new HashMap<> (Map.of(managedAttribute.getKey(), "val1")))
    .build();
    
    ObjectUpload upload = ObjectUploadFactory.newObjectUpload().fileIdentifier(objectStoreMetadata.getFileIdentifier()).build();

    objectUploadService.create(upload);
    Assertions.assertDoesNotThrow(() -> objectStoreMetaDataService.create(objectStoreMetadata));
  }
  

  @Test
  void testCreate_assignedValueNotContainedInAcceptedValues_ExceptionThrown() {
    ObjectStoreManagedAttribute managedAttribute = ObjectStoreManagedAttributeFactory.newManagedAttribute()
      .acceptedValues(new String[] {"val1", "val2"})
      .multilingualDescription(MultilingualDescriptionFactory.newMultilingualDescription().build())
      .createdBy("createdBy")
      .build();

    managedAttributeService.create(managedAttribute);
    
    ObjectStoreMetadata objectStoreMetadata = ObjectStoreMetadataFactory.newObjectStoreMetadata()
    .managedAttributeValues(new HashMap<> (Map.of(managedAttribute.getKey(), "val3")))
    .build();
    
    ObjectUpload upload = ObjectUploadFactory.newObjectUpload().fileIdentifier(objectStoreMetadata.getFileIdentifier()).build();

    objectUploadService.create(upload);
    Assertions.assertThrows(ValidationException.class, 
      () -> objectStoreMetaDataService.create(objectStoreMetadata));
  
  }

  @Test
  void testCreate_assignedKeyDoesNotExist_ExceptionThrown() {
    
    ObjectStoreMetadata objectStoreMetadata = ObjectStoreMetadataFactory.newObjectStoreMetadata()
    .managedAttributeValues(new HashMap<> (Map.of("key_x", "val3")))
    .build();
    
    ObjectUpload upload = ObjectUploadFactory.newObjectUpload().fileIdentifier(objectStoreMetadata.getFileIdentifier()).build();

    objectUploadService.create(upload);
    Assertions.assertThrows(ValidationException.class, 
      () -> objectStoreMetaDataService.create(objectStoreMetadata));

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
  
  private Derivative newDerivative(ObjectStoreMetadata child) {
    return Derivative.builder()
      .uuid(UUID.randomUUID())
      .fileIdentifier(UUID.randomUUID())
      .fileExtension(".jpg")
      .bucket("mybucket")
      .acHashValue("abc")
      .acHashFunction("abcFunction")
      .dcType(DcType.IMAGE)
      .dcFormat(MediaType.IMAGE_JPEG_VALUE)
      .createdBy(RandomStringUtils.random(4))
      .acDerivedFrom(child)
      .derivativeType(Derivative.DerivativeType.THUMBNAIL_IMAGE)
      .build();
  }

}
