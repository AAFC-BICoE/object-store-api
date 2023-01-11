package ca.gc.aafc.objectstore.api.repository;

import ca.gc.aafc.dina.vocabulary.TypedVocabularyElement;
import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreManagedAttributeDto;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.dto.ObjectSubtypeDto;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreManagedAttributeFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectSubtypeFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;
import ca.gc.aafc.objectstore.api.testsupport.fixtures.ObjectStoreManagedAttributeFixture;
import io.crnk.core.queryspec.QuerySpec;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import javax.inject.Inject;
import javax.persistence.criteria.Predicate;
import javax.validation.ValidationException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectStoreMetadataRepositoryCRUDIT extends BaseIntegrationTest {

  @Inject
  private ObjectStoreResourceRepository objectStoreResourceRepository;

  @Inject
  private ObjectStoreManagedAttributeResourceRepository managedResourceRepository;

  private ObjectSubtypeDto acSubtype;

  private ObjectUpload objectUpload;
  
  private ObjectStoreMetadata createTestObjectStoreMetadata() {
    ObjectStoreMetadata testObjectStoreMetadata = ObjectStoreMetadataFactory.newObjectStoreMetadata().fileIdentifier(objectUpload.getFileIdentifier()).build();
    objectStoreMetaDataService.create(testObjectStoreMetadata);
    return testObjectStoreMetadata;
  }
  
  private ObjectStoreManagedAttribute createTestManagedAttribute() {
    ObjectStoreManagedAttribute testManagedAttribute = ObjectStoreManagedAttributeFactory.newManagedAttribute()
    .acceptedValues(new String[]{"dorsal"})
    .build();
    return managedAttributeService.create(testManagedAttribute);
  }

  @BeforeEach
  public void setup() {
    objectUpload = createObjectUpload();
    createAcSubtype();
  }
  
  /**
   * Clean up database after each test.
   */
  @AfterEach
  public void tearDown() {
    objectStoreMetaDataService.findAll(ObjectStoreMetadata.class,
    (criteriaBuilder, objectStoreMetadataRoot) -> new Predicate[0],
    null, 0, 100).forEach(metadata -> {
      metadata.getDerivatives().forEach(derivativeService::delete);
      objectStoreMetaDataService.delete(metadata);
    });
    objectUploadService.delete(objectUpload);
  }
  
  private void createAcSubtype() {
    ObjectSubtype oSubtype = ObjectSubtypeFactory.newObjectSubtype().build();
    objectSubtypeService.create(oSubtype);
    acSubtype = new ObjectSubtypeDto();
    acSubtype.setUuid(oSubtype.getUuid());
    acSubtype.setAcSubtype(oSubtype.getAcSubtype());
    acSubtype.setDcType(oSubtype.getDcType());
  }
  
  private ObjectUpload createObjectUpload() {
    ObjectUpload newObjectUpload = ObjectUploadFactory.buildTestObjectUpload();
    objectUploadService.create(newObjectUpload);
    return newObjectUpload;
  }
  
  @Test
  public void findMeta_whenNoFieldsAreSelected_MetadataReturnedWithAllFields() {
    ObjectStoreMetadata testMetadata = createTestObjectStoreMetadata();
    ObjectStoreMetadataDto objectStoreMetadataDto = fetchMetaById(testMetadata.getUuid());
    assertNotNull(objectStoreMetadataDto);
    assertEquals(testMetadata.getUuid(), objectStoreMetadataDto.getUuid());
    assertEquals(testMetadata.getDcType(), objectStoreMetadataDto.getDcType());
    assertEquals(
        testMetadata.getAcDigitizationDate(),
      objectStoreMetadataDto.getAcDigitizationDate());
      assertEquals(
          testMetadata.getManagedAttributes(),
        objectStoreMetadataDto.getManagedAttributes());
  }

  @Test
  public void create_ValidResource_ResourcePersisted() {

    ObjectStoreManagedAttribute testManagedAttribute = createTestManagedAttribute();

    ObjectUpload objectUploadTest = ObjectUploadFactory.newObjectUpload().build();
    objectUploadService.create(objectUploadTest);

    ObjectStoreMetadataDto dto = new ObjectStoreMetadataDto();
    dto.setBucket(objectUploadTest.getBucket());
    dto.setFileIdentifier(objectUploadTest.getFileIdentifier());
    dto.setAcSubtype(acSubtype.getAcSubtype());
    dto.setDcType(acSubtype.getDcType());
    dto.setXmpRightsUsageTerms(ObjectUploadFactory.TEST_USAGE_TERMS);
    dto.setCreatedBy(RandomStringUtils.random(4));
    dto.setManagedAttributes(Map.of(testManagedAttribute.getKey(), testManagedAttribute.getAcceptedValues()[0]));

    UUID dtoUuid = objectStoreResourceRepository.create(dto).getUuid();

    ObjectStoreMetadata result = objectStoreMetaDataService.findOne(dtoUuid);
    assertEquals(dtoUuid, result.getUuid());
    assertEquals(objectUploadTest.getBucket(), result.getBucket());
    assertEquals(objectUploadTest.getFileIdentifier(), result.getFileIdentifier());
    assertEquals(acSubtype.getUuid(), result.getAcSubtype().getUuid());
    assertEquals(ObjectUploadFactory.TEST_USAGE_TERMS, result.getXmpRightsUsageTerms());
    assertEquals(testManagedAttribute.getAcceptedValues()[0],
    result.getManagedAttributes().get(testManagedAttribute.getKey()));
  }

  @Test
  public void create_resourceWithUnescapedHtmlChars_ResourcePersisted() {

    ObjectStoreManagedAttribute testManagedAttribute = ObjectStoreManagedAttributeFactory.newManagedAttribute()
            .build();
    testManagedAttribute = managedAttributeService.create(testManagedAttribute);

    ObjectUpload objectUploadTest = ObjectUploadFactory.newObjectUpload().build();
    objectUploadService.create(objectUploadTest);

    ObjectStoreMetadataDto dto = new ObjectStoreMetadataDto();
    dto.setBucket(objectUploadTest.getBucket());
    dto.setFileIdentifier(objectUploadTest.getFileIdentifier());
    dto.setAcSubtype(acSubtype.getAcSubtype());
    dto.setDcType(acSubtype.getDcType());
    dto.setXmpRightsUsageTerms(ObjectUploadFactory.TEST_USAGE_TERMS);
    dto.setCreatedBy(RandomStringUtils.random(4));
    dto.setManagedAttributes(Map.of(testManagedAttribute.getKey(), " = = < -"));

    UUID dtoUuid = objectStoreResourceRepository.create(dto).getUuid();

    ObjectStoreMetadata result = objectStoreMetaDataService.findOne(dtoUuid);
    assertEquals(dtoUuid, result.getUuid());

  }

  @Test
  public void create_OnValidExternalResource_persisted() {
    ObjectStoreMetadata testObjectStoreMetadata = ObjectStoreMetadataFactory
        .newObjectStoreMetadata()
        .fileExtension(null)
        .acHashValue(null)
        .fileIdentifier(null)
        .resourceExternalURL("https://www.perdu.com")
        .build();
    objectStoreMetaDataService.createAndFlush(testObjectStoreMetadata);

    ObjectStoreMetadata result = objectStoreMetaDataService.findOne(testObjectStoreMetadata.getUuid());

    assertNull(result.getFileExtension());
    assertNull(result.getFileIdentifier());
  }
      
  @Test
  public void create_onMetadataOnDerivative_ExceptionThrown() {
    UUID uuid = UUID.randomUUID();
    ObjectUpload derivativeObjectUpload = ObjectUploadFactory.buildTestObjectUpload();
    derivativeObjectUpload.setFileIdentifier(uuid);
    derivativeObjectUpload.setIsDerivative(true);
    objectUploadService.create(derivativeObjectUpload);

    ObjectStoreMetadataDto resource = newMetaDto();
    resource.setFileIdentifier(uuid);

    assertThrows(ValidationException.class, () -> objectStoreResourceRepository.create(resource));
    objectUploadService.delete(derivativeObjectUpload);
  }
      
  @Test
  public void create_ValidResource_ThumbNailMetaDerivesFromParent() {
    // Resource needs an detected media type that supports thumbnails
    ObjectUpload objectUpload = ObjectUploadFactory.newObjectUpload().build();
    objectUpload.setDetectedMediaType(MediaType.IMAGE_JPEG_VALUE);
    objectUploadService.create(objectUpload);
    ObjectStoreMetadataDto resource = newMetaDto();
    resource.setFileIdentifier(objectUpload.getFileIdentifier());

    UUID parentUuid = objectStoreResourceRepository.create(resource).getUuid();
    Derivative child = derivativeService.findAll(Derivative.class,
    (criteriaBuilder, root) -> new Predicate[]{criteriaBuilder.equal(
      root.get("acDerivedFrom"),
      objectStoreMetaDataService.findOne(parentUuid))},
      null, 0, 1).stream().findFirst().orElse(null);
      //Assert values
      assertNotNull(child);
      assertNotNull(child.getAcDerivedFrom());
      assertEquals(parentUuid, child.getAcDerivedFrom().getUuid());
      assertEquals(Derivative.DerivativeType.THUMBNAIL_IMAGE, child.getDerivativeType());
    }

  @Test
  public void save_ValidResource_ResourceUpdated() {
    ObjectStoreMetadata testMetadata = createTestObjectStoreMetadata();
    ObjectStoreMetadataDto updateMetadataDto = fetchMetaById(testMetadata.getUuid());
    updateMetadataDto.setBucket(ObjectUploadFactory.TEST_BUCKET);
    updateMetadataDto.setFileIdentifier(ObjectUploadFactory.TEST_FILE_IDENTIFIER);
    updateMetadataDto.setAcSubtype(acSubtype.getAcSubtype());
    updateMetadataDto.setXmpRightsUsageTerms(ObjectUploadFactory.TEST_USAGE_TERMS);

    objectStoreResourceRepository.save(updateMetadataDto);

    ObjectStoreMetadata result = objectStoreMetaDataService.findOne(updateMetadataDto.getUuid());
    assertEquals(ObjectUploadFactory.TEST_BUCKET, result.getBucket());
    assertEquals(ObjectUploadFactory.TEST_FILE_IDENTIFIER, result.getFileIdentifier());
    assertEquals(acSubtype.getUuid(), result.getAcSubtype().getUuid());
    assertEquals(ObjectUploadFactory.TEST_USAGE_TERMS, result.getXmpRightsUsageTerms());

    //Can break Relationships
    assertRelationshipsRemoved(testMetadata.getUuid());
  }

  private void assertRelationshipsRemoved(UUID metadataUUID) {
    ObjectStoreMetadataDto updateMetadataDto = fetchMetaById(metadataUUID);
    assertNotNull(updateMetadataDto.getAcSubtype());

    updateMetadataDto.setAcSubtype(null);

    objectStoreResourceRepository.save(updateMetadataDto);

    ObjectStoreMetadata result = objectStoreMetaDataService.findOne(updateMetadataDto.getUuid());
    Assertions.assertNull(result.getAcSubtype());
  }

  @Test
  public void create_onManagedAttributeValue_validationOccur() {

    ObjectStoreManagedAttributeDto newAttribute = ObjectStoreManagedAttributeFixture.newObjectStoreManagedAttribute();
    newAttribute.setVocabularyElementType(TypedVocabularyElement.VocabularyElementType.DATE);
    newAttribute.setAcceptedValues(null);
    newAttribute = managedResourceRepository.create(newAttribute);

    ObjectStoreMetadata testMetadata = createTestObjectStoreMetadata();
    ObjectStoreMetadataDto testMetadataDto = fetchMetaById(testMetadata.getUuid());

    // Put an invalid value for Date
    testMetadataDto.setManagedAttributes(Map.of(newAttribute.getKey(), "zxy"));
    assertThrows(ValidationException.class, () -> objectStoreResourceRepository.save(testMetadataDto));

    // Fix the value
    testMetadataDto.setManagedAttributes(Map.of(newAttribute.getKey(), "2022-02-02"));
    objectStoreResourceRepository.save(testMetadataDto);

    //cleanup
    objectStoreResourceRepository.delete(testMetadata.getUuid());

    // can't delete managed attribute for now since the check for key in use is using a fresh transaction
  }


  
  private ObjectStoreMetadataDto newMetaDto() {
    ObjectStoreMetadataDto parentDTO = new ObjectStoreMetadataDto();
    parentDTO.setBucket(ObjectUploadFactory.TEST_BUCKET);
    parentDTO.setFileIdentifier(ObjectUploadFactory.TEST_FILE_IDENTIFIER);
    parentDTO.setDcType(acSubtype.getDcType());
    parentDTO.setXmpRightsUsageTerms(ObjectUploadFactory.TEST_USAGE_TERMS);
    parentDTO.setCreatedBy(RandomStringUtils.random(4));
    return parentDTO;
  }
  
  private ObjectStoreMetadataDto fetchMetaById(UUID uuid) {
    return objectStoreResourceRepository.findOne(uuid, newQuery());
  }
  
  private static QuerySpec newQuery() {
    QuerySpec querySpec = new QuerySpec(ObjectStoreMetadataDto.class);
    querySpec.includeRelation(List.of("derivatives"));
    return querySpec;
  }
  
}
