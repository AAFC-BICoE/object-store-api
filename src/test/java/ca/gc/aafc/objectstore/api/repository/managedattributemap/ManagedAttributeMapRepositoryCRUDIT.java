package ca.gc.aafc.objectstore.api.repository.managedattributemap;

import ca.gc.aafc.objectstore.api.dto.ManagedAttributeDto;
import ca.gc.aafc.objectstore.api.dto.ManagedAttributeMapDto;
import ca.gc.aafc.objectstore.api.dto.ManagedAttributeMapDto.ManagedAttributeMapValue;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.repository.BaseRepositoryTest;
import ca.gc.aafc.objectstore.api.respository.ManagedAttributeResourceRepository;
import ca.gc.aafc.objectstore.api.respository.ObjectStoreResourceRepository;
import ca.gc.aafc.objectstore.api.respository.managedattributemap.ManagedAttributeMapRepository;
import ca.gc.aafc.objectstore.api.testsupport.factories.ManagedAttributeFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.MetadataManagedAttributeFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import com.google.common.collect.ImmutableMap;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.web.client.HttpClientErrorException;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.validation.ValidationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ManagedAttributeMapRepositoryCRUDIT extends BaseRepositoryTest {

  @Inject
  private ManagedAttributeMapRepository managedAttributeMapRepository;

  @Inject
  private ManagedAttributeResourceRepository managedResourceRepository;

  @Inject
  private ObjectStoreResourceRepository metadataRepository;

  @Inject
  private EntityManager entityManager;

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
  public void setAttributeValue_whenMMADoesntExist() {
    // Set attr2 value:
    managedAttributeMapRepository.create(ManagedAttributeMapDto.builder()
      .metadata(metadataRepository.findOne(
        testMetadata.getUuid(),
        new QuerySpec(ObjectStoreMetadataDto.class)))
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

  }

  @Test
  public void setAttributeValue_whenMMADoesntExist_addNewInvalidValue_validationThrowsException() {
    testManagedAttribute2.setAcceptedValues(new String[]{"acceptable test value2"});
    entityManager.persist(testManagedAttribute2);

    entityManager.flush();
    entityManager.refresh(testMetadata);
    // Set attr2 with value not in accepted values list
    assertThrows(
      InvalidDataAccessApiUsageException.class,
      () -> managedAttributeMapRepository.create(ManagedAttributeMapDto.builder()
        .metadata(metadataRepository.findOne(
          testMetadata.getUuid(),
          new QuerySpec(ObjectStoreMetadataDto.class)))
        .values(ImmutableMap.<String, ManagedAttributeMapValue>builder().put(
          testManagedAttribute2.getUuid().toString(),
          ManagedAttributeMapValue.builder().value("New attr2 value").build()).build())
        .build()));
  }

  @Test
  public void setAttributeValue_whenMMAExists_overwriteMMAWithInvalidValue_validationThrowsException() {
    testManagedAttribute1.setAcceptedValues(new String[]{"acceptable test value1"});
    entityManager.persist(testManagedAttribute1);

    entityManager.flush();
    entityManager.refresh(testMetadata);

    // Set attr1 value:
    assertThrows(InvalidDataAccessApiUsageException.class, () -> managedAttributeMapRepository.create(
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
    ));
  }

  @Test
  public void setAttributeValue_whenMMAExists_overwriteMMA_() {
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

    managedResourceRepository.delete(testManagedAttribute2.getUuid());
    Assertions.assertThrows(HttpClientErrorException.Conflict.class, () -> entityManager.flush());
  }
}
