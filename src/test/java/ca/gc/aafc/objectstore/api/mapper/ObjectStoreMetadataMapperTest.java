package ca.gc.aafc.objectstore.api.mapper;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;

import ca.gc.aafc.objectstore.api.dto.DerivativeDto;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.testsupport.factories.DerivativeFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import ca.gc.aafc.objectstore.api.testsupport.fixtures.DerivativeTestFixture;
import ca.gc.aafc.objectstore.api.testsupport.fixtures.ObjectStoreMetadataTestFixture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ObjectStoreMetadataMapperTest {

  @Test
  public void testMapperToEntity() {
    ObjectStoreMetadataDto dto = ObjectStoreMetadataTestFixture.newObjectStoreMetadata();
    dto.setOriginalFilename("allo.txt");
    dto.setManagedAttributes(Map.of("att1", "val1"));

    DerivativeDto derivativeDto = DerivativeTestFixture.newDerivative(UUID.randomUUID());

    dto.setDerivatives(List.of(derivativeDto));

    ObjectStoreMetadata
      entity = ObjectStoreMetadataMapper.INSTANCE.toEntity(dto,
      Set.of("originalFilename", "managedAttributes", "derivatives"), null);

    assertEquals(dto.getOriginalFilename(), entity.getOriginalFilename());
    assertEquals(dto.getManagedAttributes(), entity.getManagedAttributes());

    // relationships should not be mapped to entity by the mapper
    assertTrue(CollectionUtils.isEmpty(entity.getDerivatives()));
  }

  @Test
  public void testMapperToDto() {

    ObjectStoreMetadata entity = ObjectStoreMetadataFactory.newEmptyObjectStoreMetadata();
    entity.setOriginalFilename("allo.txt");
    entity.setManagedAttributes(Map.of("att1", "val1"));

    Derivative derivativeEntity = DerivativeFactory.newDerivative(entity, UUID.randomUUID()).build();
    entity.setDerivatives(List.of(derivativeEntity));

    ObjectStoreMetadataDto dto = ObjectStoreMetadataMapper.INSTANCE.toDto(entity,
      Set.of("originalFilename", "managedAttributes", "derivatives"), null);

    assertEquals(entity.getOriginalFilename(), dto.getOriginalFilename());
    assertEquals(entity.getManagedAttributes(), dto.getManagedAttributes());

    // relationships should be returned in entity -> dto mapping
    assertEquals(derivativeEntity.getFileIdentifier(), dto.getDerivatives().getFirst().getFileIdentifier());
  }

}
