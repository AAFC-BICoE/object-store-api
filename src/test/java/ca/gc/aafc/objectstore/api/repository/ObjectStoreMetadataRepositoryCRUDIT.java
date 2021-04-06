package ca.gc.aafc.objectstore.api.repository;

import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.objectstore.api.MinioTestConfiguration;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.dto.ObjectSubtypeDto;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.respository.ObjectStoreResourceRepository;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectSubtypeFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import javax.inject.Inject;
import javax.persistence.criteria.Predicate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ObjectStoreMetadataRepositoryCRUDIT extends BaseRepositoryTest {

  @Inject
  private ObjectStoreResourceRepository objectStoreResourceRepository;
  @Inject
  private DinaService<ObjectStoreMetadata> metaService;

  private ObjectStoreMetadata testObjectStoreMetadata;

  private ObjectSubtypeDto acSubType;

  private ObjectStoreMetadataDto derived;

  private ObjectUpload objectUpload;

  private void createTestObjectStoreMetadata() {
    testObjectStoreMetadata = ObjectStoreMetadataFactory.newObjectStoreMetadata().build();
    persist(testObjectStoreMetadata);
  }

  @BeforeEach
  public void setup() {
    createTestObjectStoreMetadata();
    createAcSubType();
    createDerivedFrom();
    objectUpload = createObjectUpload();
  }

  /**
   * Clean up database after each test.
   */
  @AfterEach
  public void tearDown() {
    metaService.findAll(ObjectStoreMetadata.class,
      (criteriaBuilder, objectStoreMetadataRoot) -> new Predicate[0],
      null, 0, 100).forEach(metaService::delete);
    service.deleteById(ObjectUpload.class, objectUpload.getId());
  }

  private void createAcSubType() {
    ObjectSubtype oSubtype = ObjectSubtypeFactory.newObjectSubtype().build();
    persist(oSubtype);
    acSubType = new ObjectSubtypeDto();
    acSubType.setUuid(oSubtype.getUuid());
    acSubType.setAcSubtype(oSubtype.getAcSubtype());
    acSubType.setDcType(oSubtype.getDcType());
  }

  private void createDerivedFrom() {
    ObjectStoreMetadata derivedMeta = ObjectStoreMetadataFactory.newObjectStoreMetadata().build();
    persist(derivedMeta);
    derived = new ObjectStoreMetadataDto();
    derived.setUuid(derivedMeta.getUuid());
  }

  private ObjectUpload createObjectUpload() {
    ObjectUpload newObjectUpload = MinioTestConfiguration.buildTestObjectUpload();
    persist(newObjectUpload);
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
  }

  @Test
  public void findMeta_whenManagedAttributeMapRequested_noExceptionThrown() {
    QuerySpec querySpec = new QuerySpec(ObjectStoreMetadataDto.class);
    querySpec.includeRelation(Collections.singletonList("managedAttributeMap"));

    ResourceList<ObjectStoreMetadataDto> objectStoreMetadataDto = objectStoreResourceRepository.findAll(
      querySpec);
    assertNotNull(objectStoreMetadataDto);
    // We cannot check for the presence of the ManagedAttributeMap in in this test, because Crnk
    // fetches relations marked with "LookupIncludeBehavior.AUTOMATICALLY_ALWAYS" outside of "findAll".
    // The test for this inclusion are done in MetadataToManagedAttributeMapRepositoryCRUDIT.
  }

  @Test
  public void create_ValidResource_ResourcePersisted() {
    ObjectStoreMetadataDto dto = new ObjectStoreMetadataDto();
    dto.setBucket(MinioTestConfiguration.TEST_BUCKET);
    dto.setFileIdentifier(MinioTestConfiguration.TEST_FILE_IDENTIFIER);
    dto.setAcDerivedFrom(derived);
    dto.setAcSubType(acSubType.getAcSubtype());
    dto.setDcType(acSubType.getDcType());
    dto.setXmpRightsUsageTerms(MinioTestConfiguration.TEST_USAGE_TERMS);
    dto.setCreatedBy(RandomStringUtils.random(4));

    UUID dtoUuid = objectStoreResourceRepository.create(dto).getUuid();

    ObjectStoreMetadata result = service.findUnique(ObjectStoreMetadata.class, "uuid", dtoUuid);
    assertEquals(dtoUuid, result.getUuid());
    assertEquals(MinioTestConfiguration.TEST_BUCKET, result.getBucket());
    assertEquals(MinioTestConfiguration.TEST_FILE_IDENTIFIER, result.getFileIdentifier());
    assertEquals(derived.getUuid(), result.getAcDerivedFrom().getUuid());
    assertEquals(acSubType.getUuid(), result.getAcSubType().getUuid());
    assertEquals(MinioTestConfiguration.TEST_USAGE_TERMS, result.getXmpRightsUsageTerms());

    List<ObjectStoreMetadataDto> derivatives = fetchMetaById(derived.getUuid()).getDerivatives();
    assertEquals(1, derivatives.size());
    assertEquals(result.getUuid(), derivatives.get(0).getUuid());
  }

  @Test
  public void create_ValidResource_ThumbNailMetaDerivesFromParent() {
    // Resource needs an detected media type that supports thumbnails
    ObjectUpload objectUpload = ObjectUploadFactory.newObjectUpload().build();
    objectUpload.setDetectedMediaType(MediaType.IMAGE_JPEG_VALUE);
    persist(objectUpload);
    ObjectStoreMetadataDto resource = newMetaDto();
    resource.setFileIdentifier(objectUpload.getFileIdentifier());

    UUID parentUuid = objectStoreResourceRepository.create(resource).getUuid();
    Derivative child = metaService.findAll(Derivative.class,
      (criteriaBuilder, root) -> new Predicate[]{criteriaBuilder.equal(
        root.get("acDerivedFrom"),
        metaService.findOne(parentUuid, ObjectStoreMetadata.class))},
      null, 0, 1).stream().findFirst().orElse(null);
    //Assert values
    assertNotNull(child);
    assertNotNull(child.getAcDerivedFrom());
    assertEquals(parentUuid, child.getAcDerivedFrom().getUuid());
  }

  @Test
  public void save_ValidResource_ResourceUpdated() {
    assertEquals(0, fetchMetaById(derived.getUuid()).getDerivatives().size());

    ObjectStoreMetadataDto updateMetadataDto = getDtoUnderTest();
    updateMetadataDto.setBucket(MinioTestConfiguration.TEST_BUCKET);
    updateMetadataDto.setFileIdentifier(MinioTestConfiguration.TEST_FILE_IDENTIFIER);
    updateMetadataDto.setAcDerivedFrom(derived);
    updateMetadataDto.setAcSubType(acSubType.getAcSubtype());
    updateMetadataDto.setXmpRightsUsageTerms(MinioTestConfiguration.TEST_USAGE_TERMS);

    objectStoreResourceRepository.save(updateMetadataDto);

    ObjectStoreMetadata result = service.findUnique(ObjectStoreMetadata.class, "uuid", updateMetadataDto.getUuid());
    assertEquals(MinioTestConfiguration.TEST_BUCKET, result.getBucket());
    assertEquals(MinioTestConfiguration.TEST_FILE_IDENTIFIER, result.getFileIdentifier());
    assertEquals(derived.getUuid(), result.getAcDerivedFrom().getUuid());
    assertEquals(acSubType.getUuid(), result.getAcSubType().getUuid());
    assertEquals(MinioTestConfiguration.TEST_USAGE_TERMS, result.getXmpRightsUsageTerms());

    List<ObjectStoreMetadataDto> derivatives = fetchMetaById(derived.getUuid()).getDerivatives();
    assertEquals(1, derivatives.size());
    assertEquals(result.getUuid(), derivatives.get(0).getUuid());

    //Can break Relationships
    assertRelationshipsRemoved();
  }

  private void assertRelationshipsRemoved() {
    ObjectStoreMetadataDto updateMetadataDto = getDtoUnderTest();
    assertNotNull(updateMetadataDto.getAcDerivedFrom());
    assertNotNull(updateMetadataDto.getAcSubType());

    updateMetadataDto.setAcDerivedFrom(null);
    updateMetadataDto.setAcSubType(null);

    objectStoreResourceRepository.save(updateMetadataDto);

    ObjectStoreMetadata result = service.findUnique(
      ObjectStoreMetadata.class, "uuid", updateMetadataDto.getUuid());
    Assertions.assertNull(result.getAcDerivedFrom());
    Assertions.assertNull(result.getAcSubType());
  }

  private ObjectStoreMetadataDto newMetaDto() {
    ObjectStoreMetadataDto parentDTO = new ObjectStoreMetadataDto();
    parentDTO.setBucket(MinioTestConfiguration.TEST_BUCKET);
    parentDTO.setFileIdentifier(MinioTestConfiguration.TEST_FILE_IDENTIFIER);
    parentDTO.setDcType(acSubType.getDcType());
    parentDTO.setXmpRightsUsageTerms(MinioTestConfiguration.TEST_USAGE_TERMS);
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
    querySpec.includeRelation(Collections.singletonList("acDerivedFrom"));
    return querySpec;
  }

}
