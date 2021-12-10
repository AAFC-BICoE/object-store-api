package ca.gc.aafc.objectstore.api.repository;

import ca.gc.aafc.dina.i18n.MultilingualDescription;
import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.dto.ObjectSubtypeDto;
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

  private ObjectStoreManagedAttribute testManagedAttribute;

  private ObjectStoreMetadata testObjectStoreMetadata;

  private ObjectSubtypeDto acSubtype;

  private ObjectUpload objectUpload;
  
  private void createTestObjectStoreMetadata() {
    testObjectStoreMetadata = ObjectStoreMetadataFactory.newObjectStoreMetadata().fileIdentifier(objectUpload.getFileIdentifier()).build();
    objectStoreMetaDataService.create(testObjectStoreMetadata);
  }
  
  private void createTestManagedAttribute() {
    testManagedAttribute = ObjectStoreManagedAttributeFactory.newManagedAttribute()
    .acceptedValues(new String[]{"dosal"})
    .multilingualDescription(MultilingualDescriptionFactory.newMultilingualDescription().build())
    .build();
    managedAttributeService.create(testManagedAttribute);
  }

  @BeforeEach
  public void setup() {
    objectUpload = createObjectUpload();
    createTestObjectStoreMetadata();
    createAcSubtype();
    createTestManagedAttribute();
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
    ObjectStoreMetadataDto objectStoreMetadataDto = getDtoUnderTest();
    assertNotNull(objectStoreMetadataDto);
    assertEquals(testObjectStoreMetadata.getUuid(), objectStoreMetadataDto.getUuid());
    assertEquals(testObjectStoreMetadata.getDcType(), objectStoreMetadataDto.getDcType());
    assertEquals(
      testObjectStoreMetadata.getAcDigitizationDate(),
      objectStoreMetadataDto.getAcDigitizationDate());
      assertEquals(
        testObjectStoreMetadata.getManagedAttributeValues(),
        objectStoreMetadataDto.getManagedAttributeValues());
      }
      
      @Test
      public void create_ValidResource_ResourcePersisted() {
        ObjectUpload objectUploadTest = ObjectUploadFactory.newObjectUpload().build();
        objectUploadService.create(objectUploadTest);
        
        ObjectStoreMetadataDto dto = new ObjectStoreMetadataDto();
        dto.setBucket(objectUploadTest.getBucket());
        dto.setFileIdentifier(objectUploadTest.getFileIdentifier());
        dto.setAcSubtype(acSubtype.getAcSubtype());
        dto.setDcType(acSubtype.getDcType());
        dto.setXmpRightsUsageTerms(ObjectUploadFactory.TEST_USAGE_TERMS);
        dto.setCreatedBy(RandomStringUtils.random(4));
        dto.setManagedAttributeValues(Map.of(testManagedAttribute.getKey(), testManagedAttribute.getAcceptedValues()[0]));
        
        UUID dtoUuid = objectStoreResourceRepository.create(dto).getUuid();

        ObjectStoreMetadata result = objectStoreMetaDataService.findOne(dtoUuid);
        assertEquals(dtoUuid, result.getUuid());
        assertEquals(objectUploadTest.getBucket(), result.getBucket());
        assertEquals(objectUploadTest.getFileIdentifier(), result.getFileIdentifier());
        assertEquals(acSubtype.getUuid(), result.getAcSubtype().getUuid());
        assertEquals(ObjectUploadFactory.TEST_USAGE_TERMS, result.getXmpRightsUsageTerms());
        assertEquals(testManagedAttribute.getAcceptedValues()[0],
        result.getManagedAttributeValues().get(testManagedAttribute.getKey()));
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
          ObjectStoreMetadataDto updateMetadataDto = getDtoUnderTest();
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
          assertRelationshipsRemoved();
        }
        
        private void assertRelationshipsRemoved() {
          ObjectStoreMetadataDto updateMetadataDto = getDtoUnderTest();
          assertNotNull(updateMetadataDto.getAcSubtype());
          
    updateMetadataDto.setAcSubtype(null);
    
    objectStoreResourceRepository.save(updateMetadataDto);
    
    ObjectStoreMetadata result = objectStoreMetaDataService.findOne(updateMetadataDto.getUuid());
    Assertions.assertNull(result.getAcSubtype());
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
  
  private ObjectStoreMetadataDto getDtoUnderTest() {
    return fetchMetaById(testObjectStoreMetadata.getUuid());
  }
  
  private static QuerySpec newQuery() {
    QuerySpec querySpec = new QuerySpec(ObjectStoreMetadataDto.class);
    querySpec.includeRelation(List.of("derivatives"));
    return querySpec;
  }
  
}
