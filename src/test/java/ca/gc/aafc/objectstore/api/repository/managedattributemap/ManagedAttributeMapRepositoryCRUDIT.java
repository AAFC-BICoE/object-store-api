package ca.gc.aafc.objectstore.api.repository.managedattributemap;

import ca.gc.aafc.objectstore.api.MinioTestConfiguration;
import ca.gc.aafc.objectstore.api.dto.ManagedAttributeMapDto;
import ca.gc.aafc.objectstore.api.dto.ManagedAttributeMapDto.ManagedAttributeMapValue;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.repository.BaseRepositoryTest;
import ca.gc.aafc.objectstore.api.respository.ObjectStoreResourceRepository;
import ca.gc.aafc.objectstore.api.respository.managedattributemap.ManagedAttributeMapRepository;
import ca.gc.aafc.objectstore.api.testsupport.factories.ManagedAttributeFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.MetadataManagedAttributeFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectSubtypeFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import org.apache.commons.lang3.RandomStringUtils;
import org.javers.core.Javers;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.validation.ValidationException;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ManagedAttributeMapRepositoryCRUDIT extends BaseRepositoryTest {

  @Inject
  private ManagedAttributeMapRepository managedAttributeMapRepository;

  @Inject
  private ObjectStoreResourceRepository metadataRepository;

  @Inject
  private EntityManager entityManager;

  @Inject
  private Javers javers;

  @Inject
  private ObjectMapper objectMapper;

  private ObjectStoreMetadata testMetadata;
  private ManagedAttribute testManagedAttribute1;
  private ManagedAttribute testManagedAttribute2;
  private MetadataManagedAttribute testAttr1Value;

  @BeforeEach
  public void setup() {
    testMetadata = ObjectStoreMetadataFactory.newObjectStoreMetadata().build();
    entityManager.persist(testMetadata);

    testManagedAttribute1 = ManagedAttributeFactory.newManagedAttribute().name("attr1").build();
    entityManager.persist(testManagedAttribute1);

    testManagedAttribute2 = ManagedAttributeFactory.newManagedAttribute().name("attr2").build();
    entityManager.persist(testManagedAttribute2);

    testAttr1Value = MetadataManagedAttributeFactory.newMetadataManagedAttribute()
      .assignedValue("test value 1")
      .managedAttribute(testManagedAttribute1)
      .objectStoreMetadata(testMetadata)
      .build();
    entityManager.persist(testAttr1Value);

    entityManager.flush();
    entityManager.refresh(testMetadata);
  }

  @Test
  public void setAttributeValue_whenMMADoesntExist_createMMAAndSnapshot() {//TODO need to evaluate what is being tested and why it fails
    ObjectSubtype oSubtype = ObjectSubtypeFactory.newObjectSubtype().build();
    persist(oSubtype);
    ObjectUpload newObjectUpload = MinioTestConfiguration.buildTestObjectUpload();
    persist(newObjectUpload);
    ObjectStoreMetadataDto metaDTO = new ObjectStoreMetadataDto();
    metaDTO.setFileIdentifier(newObjectUpload.getFileIdentifier());
    metaDTO.setBucket("b");
    metaDTO.setFileExtension(".jpg");
    metaDTO.setDcType(oSubtype.getDcType());
    metaDTO.setAcSubType(oSubtype.getAcSubtype());
    metaDTO.setCreatedBy(RandomStringUtils.random(4));
    UUID id = metadataRepository.create(metaDTO).getUuid();
    metaDTO.setUuid(id);

    // Set attr2 value:
    managedAttributeMapRepository.create(ManagedAttributeMapDto.builder()
      .metadata(metaDTO)
      .values(ImmutableMap.<String, ManagedAttributeMapValue>builder().put(
        testManagedAttribute2.getUuid().toString(),
        ManagedAttributeMapValue.builder().value("New attr2 value").build()).build())
      .build());

    // The managed attribute value (MetadataManagedAttribute) should have been
    // created:
    ObjectStoreMetadataDto result = metadataRepository.findOne(metaDTO.getUuid(), getQuerySpec());
    assertEquals(2, result.getManagedAttribute().size());
    assertEquals("New attr2 value", result.getManagedAttribute().get(1).getAssignedValue());

    // Check the snapshot to make sure the embedded managedAttributeMap was updated:
    CdoSnapshot latestSnapshot = javers.getLatestSnapshot(
      result.getUuid(),
      ObjectStoreMetadataDto.class
    ).get();
    // Correct fields should be updated:
    assertEquals(
      Arrays.asList("managedAttributeMap", "createdOn"),
      latestSnapshot.getChanged()
    );

    ManagedAttributeMapDto attrMap = objectMapper.convertValue(
      latestSnapshot.getPropertyValue("managedAttributeMap"),
      ManagedAttributeMapDto.class
    );
    assertEquals(
      "test value 1",
      attrMap.getValues().get(testManagedAttribute1.getUuid().toString()).getValue()
    );
    assertEquals(
      "New attr2 value",
      attrMap.getValues().get(testManagedAttribute2.getUuid().toString()).getValue()
    );
  }

  @NotNull
  private QuerySpec getQuerySpec() {
    QuerySpec querySpec = new QuerySpec(ObjectStoreMetadataDto.class);
    querySpec.includeRelation(PathSpec.of("managedAttribute"));
    return querySpec;
  }

  @Test
  public void setAttributeValue_whenMMAExists_overwriteMMA() {
    // Set attr1 value:
    managedAttributeMapRepository.create(
      ManagedAttributeMapDto.builder()
        .metadata(metadataRepository.findOne(
          testMetadata.getUuid(),
          new QuerySpec(ObjectStoreMetadataDto.class)))
        .values(ImmutableMap.<String, ManagedAttributeMapValue>builder()
          .put(testManagedAttribute1.getUuid().toString(), ManagedAttributeMapValue.builder()
            .value("New attr1 value")
            .build())
          .build())
        .build()
    );

    // The managed attribute value (MetadataManagedAttribute) should have been changed:
    assertEquals(1, testMetadata.getManagedAttribute().size());
    assertEquals("New attr1 value", testMetadata.getManagedAttribute().get(0).getAssignedValue());
  }

  @Test
  public void setAttributeValueToNull_whenMMAExists_MMADeleted() {
    // Set attr1 value to null:
    managedAttributeMapRepository.create(
      ManagedAttributeMapDto.builder()
        .metadata(metadataRepository.findOne(
          testMetadata.getUuid(),
          new QuerySpec(ObjectStoreMetadataDto.class)))
        .values(ImmutableMap.<String, ManagedAttributeMapValue>builder()
          .put(testManagedAttribute1.getUuid().toString(), ManagedAttributeMapValue.builder()
            .value(null)
            .build())
          .build())
        .build()
    );

    entityManager.flush();
    entityManager.refresh(testMetadata);

    // The managed attribute value (MetadataManagedAttribute) should have been deleted:
    assertEquals(0, testMetadata.getManagedAttribute().size());
  }

  @Test
  public void setAttributeValue_whenMetadataNotSpecified_throwValidationException() {
    assertThrows(ValidationException.class, () -> {
      managedAttributeMapRepository.create(
        ManagedAttributeMapDto.builder()
          // Do not specify metadata:
          .values(ImmutableMap.<String, ManagedAttributeMapValue>builder()
            .put(testManagedAttribute1.getUuid().toString(), ManagedAttributeMapValue.builder()
              .value("New attr1 value")
              .build())
            .build())
          .build()
      );
    });
  }

}
