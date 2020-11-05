package ca.gc.aafc.objectstore.api.rest;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.MinioTestConfiguration;
import ca.gc.aafc.objectstore.api.dto.ManagedAttributeDto;
import ca.gc.aafc.objectstore.api.dto.ManagedAttributeMapDto;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.respository.ManagedAttributeResourceRepository;
import ca.gc.aafc.objectstore.api.respository.ObjectStoreResourceRepository;
import ca.gc.aafc.objectstore.api.respository.managedattributemap.ManagedAttributeMapRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.javers.core.Javers;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ManagedAttributeAuditingIT extends BaseIntegrationTest {

  @Inject
  private ManagedAttributeMapRepository managedAttributeMapRepository;

  @Inject
  private ObjectStoreResourceRepository metadataRepository;

  @Inject
  private ManagedAttributeResourceRepository managedRepo;

  @Inject
  private Javers javers;

  @Inject
  private ObjectMapper objectMapper;

  private ObjectUpload objectUpload;
  private ManagedAttributeDto managedAttribute;

  @BeforeEach
  void setUp() {
    objectUpload = MinioTestConfiguration.buildTestObjectUpload();
    service.save(objectUpload);

    ManagedAttributeDto managed = new ManagedAttributeDto();
    managed.setName("name");
    managed.setUuid(UUID.randomUUID());
    managed.setDescription(ImmutableMap.of("en", "en"));
    managed.setManagedAttributeType(ManagedAttribute.ManagedAttributeType.STRING);

    managedAttribute = managedRepo.create(managed);
  }

  @Test
  void createMMA_SnapShotPersisted() {
    String test_value_1 = "TEST_VALUE_1";
    ObjectStoreMetadataDto meta = metadataRepository.create(newMeta());

    managedAttributeMapRepository.create(newAttribMap(test_value_1, meta));

    ObjectStoreMetadataDto result = findMetaData(meta.getUuid());
    assertEquals(1, result.getManagedAttribute().size());
    assertEquals(test_value_1, result.getManagedAttribute().get(0).getAssignedValue());

    CdoSnapshot resultSnap = javers.getLatestSnapshot(meta.getUuid(), ObjectStoreMetadataDto.class)
      .orElse(null);
    Assertions.assertNotNull(resultSnap);
    MatcherAssert.assertThat(resultSnap.getChanged(), Matchers.contains("managedAttributeMap"));

    String resultAttribValue = parseMapFromSnapshot(resultSnap)
      .getValues()
      .get(managedAttribute.getUuid().toString())
      .getValue();
    Assertions.assertEquals(test_value_1, resultAttribValue);
  }

  @Test
  void update_WhenMetaDataRepo_ManagedAttributesNotAudited() {
    ObjectStoreMetadataDto meta = metadataRepository.create(newMeta());
    managedAttributeMapRepository.create(newAttribMap("value", meta));

    metadataRepository.save(findMetaData(meta.getUuid()));

    CdoSnapshot resultSnap = javers.getLatestSnapshot(meta.getUuid(), ObjectStoreMetadataDto.class)
      .orElse(null);
    Assertions.assertNotNull(resultSnap);
    MatcherAssert.assertThat(resultSnap.getChanged(), Matchers.empty());
  }

  private ManagedAttributeMapDto parseMapFromSnapshot(CdoSnapshot resultSnap) {
    return objectMapper.convertValue(
      resultSnap.getPropertyValue("managedAttributeMap"),
      ManagedAttributeMapDto.class);
  }

  private ObjectStoreMetadataDto newMeta() {
    ObjectStoreMetadataDto dto = new ObjectStoreMetadataDto();
    dto.setFileIdentifier(objectUpload.getFileIdentifier());
    dto.setBucket("b");
    return dto;
  }

  private ManagedAttributeMapDto newAttribMap(String value, ObjectStoreMetadataDto meta) {
    return ManagedAttributeMapDto.builder()
      .metadata(meta)
      .values(ImmutableMap.of(
        managedAttribute.getUuid().toString(),
        ManagedAttributeMapDto.ManagedAttributeMapValue.builder().value(value).build()))
      .build();
  }

  private ObjectStoreMetadataDto findMetaData(UUID uuid) {
    QuerySpec querySpec = new QuerySpec(ObjectStoreMetadataDto.class);
    querySpec.includeRelation(PathSpec.of("managedAttribute"));
    return metadataRepository.findOne(uuid, querySpec);
  }

}
