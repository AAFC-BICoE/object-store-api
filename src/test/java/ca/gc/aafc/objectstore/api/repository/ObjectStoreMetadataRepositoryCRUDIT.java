package ca.gc.aafc.objectstore.api.repository;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.gc.aafc.dina.exception.ResourceGoneException;
import ca.gc.aafc.dina.exception.ResourceNotFoundException;
import ca.gc.aafc.dina.jsonapi.JsonApiDocument;
import ca.gc.aafc.dina.jsonapi.JsonApiDocuments;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.dina.util.UUIDHelper;
import ca.gc.aafc.dina.vocabulary.TypedVocabularyElement;
import ca.gc.aafc.objectstore.api.config.AsyncOverrideConfig;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreManagedAttributeDto;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.dto.ObjectSubtypeDto;
import ca.gc.aafc.objectstore.api.entities.DcType;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.minio.MinioClient;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;
import javax.persistence.criteria.Predicate;
import javax.validation.ValidationException;

@Import(AsyncOverrideConfig.class)
public class ObjectStoreMetadataRepositoryCRUDIT extends ObjectStoreModuleBaseRepositoryIT {

  private static final String BASE_URL = "/api/v1/" + ObjectStoreMetadataDto.TYPENAME;

  @MockBean
  private MinioClient minioClient;

  @Autowired
  private WebApplicationContext wac;

  private MockMvc mockMvc;

  @Inject
  private ObjectStoreMetadataRepositoryV2 objectStoreResourceRepository;

  @Inject
  private ObjectStoreManagedAttributeResourceRepository managedResourceRepository;

  @Autowired
  protected ObjectStoreMetadataRepositoryCRUDIT(ObjectMapper objMapper) {
    super(BASE_URL, objMapper);
  }

  @Override
  protected MockMvc getMockMvc() {
    return mockMvc;
  }

  @BeforeEach
  public void setup() throws JsonProcessingException {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
  }

  private ObjectStoreMetadata createTestObjectStoreMetadata(UUID fileIdentifier) {
    ObjectStoreMetadata testObjectStoreMetadata = ObjectStoreMetadataFactory.newObjectStoreMetadata().fileIdentifier(fileIdentifier).build();
    objectStoreMetaDataService.create(testObjectStoreMetadata);
    return testObjectStoreMetadata;
  }
  
  private ObjectStoreManagedAttribute createTestManagedAttribute() {
    ObjectStoreManagedAttribute testManagedAttribute = ObjectStoreManagedAttributeFactory.newManagedAttribute()
    .acceptedValues(new String[]{"dorsal"})
    .build();
    return managedAttributeService.create(testManagedAttribute);
  }
  
  /**
   * Clean up database after each test.
   */
  @AfterEach
  public void tearDown() {
    objectStoreMetaDataService.findAll(ObjectStoreMetadata.class,
    (criteriaBuilder, objectStoreMetadataRoot) -> new Predicate[0],
    null, 0, 100).forEach(metadata -> {

      List<UUID> toDelete = metadata.getDerivatives().stream().map(Derivative::getUuid).toList();
      for(UUID curr : toDelete) {
        derivativeService.delete(derivativeService.findOne(curr, Derivative.class));
      }

      objectStoreMetaDataService.delete(metadata);
    });
  }
  
  private ObjectSubtypeDto createAcSubtype() {
    ObjectSubtype oSubtype = ObjectSubtypeFactory.newObjectSubtype().build();
    objectSubtypeService.create(oSubtype);
    ObjectSubtypeDto acSubtype = new ObjectSubtypeDto();
    acSubtype.setUuid(oSubtype.getUuid());
    acSubtype.setAcSubtype(oSubtype.getAcSubtype());
    acSubtype.setDcType(oSubtype.getDcType());

    return acSubtype;
  }
  
  private ObjectUpload createObjectUpload() {
    ObjectUpload newObjectUpload = ObjectUploadFactory.buildTestObjectUpload();
    objectUploadService.create(newObjectUpload);
    return newObjectUpload;
  }
  
  @Test
  public void findMeta_whenNoFieldsAreSelected_MetadataReturnedWithAllFields() throws ResourceGoneException, ResourceNotFoundException {
    ObjectUpload objectUpload = createObjectUpload();
    ObjectStoreMetadata testMetadata = createTestObjectStoreMetadata(objectUpload.getFileIdentifier());
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

    // cleanup
    objectUploadService.delete(objectUpload);
  }

  @Test
  public void create_ValidResource_ResourcePersisted() {

    ObjectStoreManagedAttribute testManagedAttribute = createTestManagedAttribute();
    ObjectSubtypeDto acSubtype = createAcSubtype();

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

    JsonApiDocument docToCreate = dtoToJsonApiDocument(dto);
    UUID dtoUuid = objectStoreResourceRepository.create(docToCreate, null).getDto().getJsonApiId();

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

    ObjectSubtypeDto acSubtype = createAcSubtype();

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

    JsonApiDocument docToCreate = dtoToJsonApiDocument(dto);
    UUID dtoUuid = objectStoreResourceRepository.create(docToCreate, null).getDto().getJsonApiId();

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
    UUID uuid = UUIDHelper.generateUUIDv7();
    ObjectUpload derivativeObjectUpload = ObjectUploadFactory.buildTestObjectUpload();
    derivativeObjectUpload.setFileIdentifier(uuid);
    derivativeObjectUpload.setIsDerivative(true);
    objectUploadService.create(derivativeObjectUpload);

    ObjectSubtypeDto acSubtype = createAcSubtype();
    ObjectStoreMetadataDto dto = newMetaDto(acSubtype.getDcType());
    dto.setFileIdentifier(uuid);

    JsonApiDocument docToCreate = dtoToJsonApiDocument(dto);
    assertThrows(ValidationException.class, () -> objectStoreResourceRepository.create(docToCreate, null));
    objectUploadService.delete(derivativeObjectUpload);
  }
      
  @Test
  public void create_ValidResource_ThumbnailMetaDerivesFromParent() {
    // Resource needs an detected media type that supports thumbnails
    ObjectUpload objectUpload = ObjectUploadFactory.newObjectUpload().build();
    objectUpload.setDetectedMediaType(MediaType.IMAGE_JPEG_VALUE);
    objectUploadService.create(objectUpload);

    ObjectSubtypeDto acSubtype = createAcSubtype();
    ObjectStoreMetadataDto dto = newMetaDto(acSubtype.getDcType());
    dto.setFileIdentifier(objectUpload.getFileIdentifier());

    JsonApiDocument docToCreate = dtoToJsonApiDocument(dto);

    UUID parentUuid = objectStoreResourceRepository.create(docToCreate, null).getDto().getJsonApiId();
    Derivative child = derivativeService.findAll(Derivative.class,
      (criteriaBuilder, root) -> new Predicate[] {criteriaBuilder.equal(
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
  public void create_validExternalResource_NoThumbnailCreated() {
    ObjectSubtypeDto acSubtype = createAcSubtype();
    ObjectStoreMetadataDto dto = newMetaDtoExternalResource(acSubtype.getDcType());

    JsonApiDocument docToCreate = dtoToJsonApiDocument(dto);

    UUID resourceUuid = objectStoreResourceRepository.create(docToCreate, null).getDto().getJsonApiId();
    Derivative child = derivativeService.findAll(Derivative.class,
      (criteriaBuilder, root) -> new Predicate[] {criteriaBuilder.equal(
        root.get("acDerivedFrom"),
        objectStoreMetaDataService.findOne(resourceUuid))},
      null, 0, 1).stream().findFirst().orElse(null);
    assertNull(child);
  }

  @Test
  public void save_ValidResource_ResourceUpdated() throws ResourceGoneException, ResourceNotFoundException {
    ObjectSubtypeDto acSubtype = createAcSubtype();
    ObjectUpload objectUpload = createObjectUpload();

    ObjectStoreMetadata testMetadata = createTestObjectStoreMetadata(objectUpload.getFileIdentifier());
    ObjectStoreMetadataDto updateMetadataDto = fetchMetaById(testMetadata.getUuid());
    updateMetadataDto.setBucket(ObjectUploadFactory.TEST_BUCKET);
    updateMetadataDto.setFileIdentifier(ObjectUploadFactory.TEST_FILE_IDENTIFIER);
    updateMetadataDto.setAcSubtype(acSubtype.getAcSubtype());
    updateMetadataDto.setXmpRightsUsageTerms(ObjectUploadFactory.TEST_USAGE_TERMS);

    JsonApiDocument docToUpdate = dtoToJsonApiDocument(updateMetadataDto);
    objectStoreResourceRepository.update(docToUpdate);

    ObjectStoreMetadata result = objectStoreMetaDataService.findOne(updateMetadataDto.getUuid());
    assertEquals(ObjectUploadFactory.TEST_BUCKET, result.getBucket());
    assertEquals(ObjectUploadFactory.TEST_FILE_IDENTIFIER, result.getFileIdentifier());
    assertEquals(acSubtype.getUuid(), result.getAcSubtype().getUuid());
    assertEquals(ObjectUploadFactory.TEST_USAGE_TERMS, result.getXmpRightsUsageTerms());

    //Can break Relationships
    assertRelationshipsRemoved(testMetadata.getUuid());

    // cleanup
    objectUploadService.delete(objectUpload);
  }

  private void assertRelationshipsRemoved(UUID metadataUUID) throws ResourceGoneException, ResourceNotFoundException {
    ObjectStoreMetadataDto updateMetadataDto = fetchMetaById(metadataUUID);
    assertNotNull(updateMetadataDto.getAcSubtype());

    updateMetadataDto.setAcSubtype(null);

    JsonApiDocument docToUpdate = dtoToJsonApiDocument(updateMetadataDto);
    objectStoreResourceRepository.update(docToUpdate);

    ObjectStoreMetadata result = objectStoreMetaDataService.findOne(updateMetadataDto.getUuid());
    assertNull(result.getAcSubtype());
  }

  @Test
  public void create_onManagedAttributeValue_validationOccur() throws ResourceGoneException, ResourceNotFoundException {

    ObjectStoreManagedAttributeDto newAttribute = ObjectStoreManagedAttributeFixture.newObjectStoreManagedAttribute();
    newAttribute.setVocabularyElementType(TypedVocabularyElement.VocabularyElementType.DATE);
    newAttribute.setAcceptedValues(null);

    JsonApiDocument docToCreate = JsonApiDocuments.createJsonApiDocument(
      null, ObjectStoreManagedAttributeDto.TYPENAME,
      JsonAPITestHelper.toAttributeMap(newAttribute)
    );
    newAttribute = managedResourceRepository.create(docToCreate, null).getDto();

    ObjectUpload objectUpload = createObjectUpload();
    ObjectStoreMetadata testMetadata = createTestObjectStoreMetadata(objectUpload.getFileIdentifier());
    ObjectStoreMetadataDto testMetadataDto = fetchMetaById(testMetadata.getUuid());

    // Put an invalid value for Date
    testMetadataDto.setManagedAttributes(Map.of(newAttribute.getKey(), "zxy"));

    JsonApiDocument docToUpdate = dtoToJsonApiDocument(testMetadataDto);
    assertThrows(ValidationException.class, () -> objectStoreResourceRepository.update(docToUpdate));

    // Fix the value
    testMetadataDto.setManagedAttributes(Map.of(newAttribute.getKey(), "2022-02-02"));

    JsonApiDocument docToReUpdate = dtoToJsonApiDocument(testMetadataDto);
    objectStoreResourceRepository.update(docToReUpdate);

    //cleanup
    objectStoreResourceRepository.delete(testMetadata.getUuid());

    // can't delete managed attribute for now since the check for key in use is using a fresh transaction

    // cleanup
    objectUploadService.delete(objectUpload);
  }

  private ObjectStoreMetadataDto newMetaDto(DcType dcType) {
    ObjectStoreMetadataDto parentDTO = new ObjectStoreMetadataDto();
    parentDTO.setBucket(ObjectUploadFactory.TEST_BUCKET);
    parentDTO.setFileIdentifier(ObjectUploadFactory.TEST_FILE_IDENTIFIER);
    parentDTO.setDcType(dcType);
    parentDTO.setXmpRightsUsageTerms(ObjectUploadFactory.TEST_USAGE_TERMS);
    parentDTO.setCreatedBy(RandomStringUtils.random(4));
    return parentDTO;
  }

  private ObjectStoreMetadataDto newMetaDtoExternalResource(DcType dcType) {
    ObjectStoreMetadataDto resource = newMetaDto(dcType);
    resource.setResourceExternalURL("https://perdu.com");
    resource.setDcFormat(MediaType.IMAGE_JPEG_VALUE);
    resource.setFileIdentifier(null);
    return resource;
  }

  private ObjectStoreMetadataDto fetchMetaById(UUID uuid) throws ResourceGoneException, ResourceNotFoundException {
    return objectStoreResourceRepository.getOne(uuid, "include=derivatives").getDto();
  }
  
}
