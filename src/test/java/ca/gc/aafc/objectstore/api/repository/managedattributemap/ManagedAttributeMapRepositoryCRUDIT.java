package ca.gc.aafc.objectstore.api.repository.managedattributemap;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.dto.ManagedAttributeMapDto;
import ca.gc.aafc.objectstore.api.dto.ManagedAttributeMapDto.ManagedAttributeMapValue;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.exceptionmapping.ManagedAttributeChildConflictException;
import ca.gc.aafc.objectstore.api.respository.ManagedAttributeResourceRepository;
import ca.gc.aafc.objectstore.api.respository.ObjectStoreResourceRepository;
import ca.gc.aafc.objectstore.api.respository.managedattributemap.ManagedAttributeMapRepository;
import ca.gc.aafc.objectstore.api.testsupport.factories.ManagedAttributeFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.MetadataManagedAttributeFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;

import com.google.common.collect.ImmutableMap;
import io.crnk.core.queryspec.QuerySpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.validation.ValidationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

public class ManagedAttributeMapRepositoryCRUDIT extends BaseIntegrationTest {

  @Inject
  private ManagedAttributeMapRepository managedAttributeMapRepository;

  @Inject
  private ManagedAttributeResourceRepository managedResourceRepository;

  @Inject
  private ObjectStoreResourceRepository metadataRepository;

  @Inject
  private EntityManager entityManager;

  private ObjectStoreMetadata testMetadata;
  private ObjectUpload testObjectUpload;
  private ManagedAttribute testManagedAttribute1;
  private ManagedAttribute testManagedAttribute2;
  private MetadataManagedAttribute testAttr1Value;

  private UUID uuid = UUID.randomUUID();

  @BeforeEach
  public void setup() {
    
    testObjectUpload = ObjectUploadFactory.newObjectUpload().fileIdentifier(uuid).build();
    objectUploadService.create(testObjectUpload);

    testMetadata = ObjectStoreMetadataFactory.newObjectStoreMetadata().fileIdentifier(uuid).build();
    objectStoreMetaDataService.create(testMetadata);


    testManagedAttribute1 = ManagedAttributeFactory.newManagedAttribute().name("attr1").build();
    managedAttributeService.create(testManagedAttribute1);

    testManagedAttribute2 = ManagedAttributeFactory.newManagedAttribute().name("attr2").build();
    managedAttributeService.create(testManagedAttribute2);

    testAttr1Value = MetadataManagedAttributeFactory.newMetadataManagedAttribute().assignedValue("test value 1")
      .managedAttribute(testManagedAttribute1).objectStoreMetadata(testMetadata).build();
    metaManagedAttributeService.create(testAttr1Value);

    entityManager.flush();
    entityManager.refresh(testMetadata);
  }

  @Test
  public void setAttributeValue_whenMMADoesntExist() {
    // Set attr2 value:
    managedAttributeMapRepository.create(ManagedAttributeMapDto.builder()
      .metadata(metadataRepository.findOne(testMetadata.getUuid(), new QuerySpec(ObjectStoreMetadataDto.class)))
      .values(ImmutableMap.<String, ManagedAttributeMapValue>builder().put(testManagedAttribute2.getUuid().toString(),
        ManagedAttributeMapValue.builder().value("New attr2 value").build()).build())
      .build());

    entityManager.flush();
    entityManager.refresh(testMetadata);

    // The managed attribute value (MetadataManagedAttribute) should have been
    // created:
    assertEquals(2, testMetadata.getManagedAttribute().size());
    assertEquals("New attr2 value", testMetadata.getManagedAttribute().get(1).getAssignedValue());

  }

  @Test
  public void setAttributeValue_whenMMADoesntExist_addNewInvalidValue_validationThrowsException() {
    testManagedAttribute2.setAcceptedValues(new String[]{"acceptable test value2"});
    managedAttributeService.create(testManagedAttribute2);

    entityManager.flush();
    entityManager.refresh(testMetadata);

    // Set attr2 with value not in accepted values list
    assertThrows(InvalidDataAccessApiUsageException.class, ()-> managedAttributeMapRepository.create(ManagedAttributeMapDto.builder()
      .metadata(metadataRepository.findOne(testMetadata.getUuid(), new QuerySpec(ObjectStoreMetadataDto.class)))
      .values(ImmutableMap.<String, ManagedAttributeMapValue>builder().put(testManagedAttribute2.getUuid().toString(),
        ManagedAttributeMapValue.builder().value("New attr2 value").build()).build())
      .build()));
  }

  @Test
  public void setAttributeValue_whenMMAExists_overwriteMMAWithInvalidValue_validationThrowsException() {
    testManagedAttribute1.setAcceptedValues(new String[]{"acceptable test value1"});
    managedAttributeService.create(testManagedAttribute1);

    entityManager.flush();
    entityManager.refresh(testMetadata);

    // Set attr1 value:
    assertThrows(InvalidDataAccessApiUsageException.class, ()-> managedAttributeMapRepository.create(
      ManagedAttributeMapDto.builder()
        .metadata(metadataRepository.findOne(testMetadata.getUuid(), new QuerySpec(ObjectStoreMetadataDto.class)))
        .values(ImmutableMap.<String, ManagedAttributeMapValue>builder()
          .put(testManagedAttribute1.getUuid().toString(), ManagedAttributeMapValue.builder()
            .value("New attr1 value")
            .build())
          .build())
        .build()
    ));
  }

  @Test
  public void setAttributeValue_whenMMAExists_overwriteMMA_() {
    // Set attr1 value:
    managedAttributeMapRepository.create(
      ManagedAttributeMapDto.builder()
        .metadata(metadataRepository.findOne(testMetadata.getUuid(), new QuerySpec(ObjectStoreMetadataDto.class)))
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
        .metadata(metadataRepository.findOne(testMetadata.getUuid(), new QuerySpec(ObjectStoreMetadataDto.class)))
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

  @Test
  void delete_WhenHasChildren_ReturnsConflict() {
    ObjectStoreMetadataDto metadata = metadataRepository.findOne(
      testMetadata.getUuid(),
      new QuerySpec(ObjectStoreMetadataDto.class));

    managedAttributeMapRepository.create(ManagedAttributeMapDto.builder()
      .metadata(metadata)
      .values(ImmutableMap.<String, ManagedAttributeMapValue>builder().put(
        testManagedAttribute2.getUuid().toString(),
        ManagedAttributeMapValue.builder().value("New attr2 value").build()).build())
      .build());

    entityManager.flush();
    entityManager.refresh(testMetadata);

    // The managed attribute value (MetadataManagedAttribute) should have been
    // created:
    assertEquals(2, testMetadata.getManagedAttribute().size());
    assertEquals("New attr2 value", testMetadata.getManagedAttribute().get(1).getAssignedValue());

    Assertions.assertThrows(
      ManagedAttributeChildConflictException.class,
      () -> managedResourceRepository.delete(testManagedAttribute2.getUuid()));
  }
}
