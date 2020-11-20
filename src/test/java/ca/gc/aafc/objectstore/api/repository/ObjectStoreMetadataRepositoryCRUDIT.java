package ca.gc.aafc.objectstore.api.repository;

import ca.gc.aafc.objectstore.api.MinioTestConfiguration;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.dto.ObjectSubtypeDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.file.ThumbnailService;
import ca.gc.aafc.objectstore.api.respository.ObjectStoreResourceRepository;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectSubtypeFactory;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ObjectStoreMetadataRepositoryCRUDIT extends BaseRepositoryTest {

  @Inject
  private ObjectStoreResourceRepository objectStoreResourceRepository;

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

  }

  @Test
  public void create_ValidResource_ThumbNailMetaDerivesFromParent() {
    ObjectStoreMetadataDto parentDTO = newMetaDto();

    UUID parentUuid = objectStoreResourceRepository.create(parentDTO).getUuid();

    ObjectStoreMetadata thumbNailMetaResult = service.findUnique(
      ObjectStoreMetadata.class,
      "fileIdentifier",
      MinioTestConfiguration.TEST_THUMBNAIL_IDENTIFIER);

    assertEquals(MinioTestConfiguration.TEST_BUCKET, thumbNailMetaResult.getBucket());
    assertEquals(MinioTestConfiguration.TEST_THUMBNAIL_IDENTIFIER, thumbNailMetaResult.getFileIdentifier());
    assertEquals(parentUuid, thumbNailMetaResult.getAcDerivedFrom().getUuid());
    assertEquals(ThumbnailService.THUMBNAIL_AC_SUB_TYPE, thumbNailMetaResult.getAcSubType().getAcSubtype());
    assertEquals(ThumbnailService.THUMBNAIL_DC_TYPE, thumbNailMetaResult.getAcSubType().getDcType());
    assertEquals(MinioTestConfiguration.TEST_USAGE_TERMS, thumbNailMetaResult.getXmpRightsUsageTerms());
  }

  @Test
  public void create_ThumbNailReturnedAsDerivative() {
    ObjectStoreMetadataDto parentDTO = newMetaDto();

    UUID parentUuid = objectStoreResourceRepository.create(parentDTO).getUuid();

    ObjectStoreMetadata child = service.findUnique(
      ObjectStoreMetadata.class,
      "fileIdentifier",
      MinioTestConfiguration.TEST_THUMBNAIL_IDENTIFIER);

    QuerySpec querySpec = new QuerySpec(ObjectStoreMetadataDto.class);
    querySpec.includeRelation(Collections.singletonList("derivatives"));

    ObjectStoreMetadataDto fetchedParent =
      objectStoreResourceRepository.findOne(parentUuid, querySpec);
    assertEquals(1, fetchedParent.getDerivatives().size());
    assertEquals(child.getUuid(), fetchedParent.getDerivatives().get(0).getUuid());
  }

  @Test
  public void save_ValidResource_ResourceUpdated() {

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
    assertNull(result.getAcDerivedFrom());
    assertNull(result.getAcSubType());
  }

  private ObjectUpload createObjectUpload() {
    ObjectUpload newObjectUpload = MinioTestConfiguration.buildTestObjectUpload();
    persist(newObjectUpload);
    return newObjectUpload;
  }

  private ObjectStoreMetadataDto getDtoUnderTest() {
    QuerySpec querySpec = new QuerySpec(ObjectStoreMetadataDto.class);
    querySpec.includeRelation(Collections.singletonList("acDerivedFrom"));
    return objectStoreResourceRepository.findOne(testObjectStoreMetadata.getUuid(), querySpec);
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

}
