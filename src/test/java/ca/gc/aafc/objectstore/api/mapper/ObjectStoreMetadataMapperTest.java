package ca.gc.aafc.objectstore.api.mapper;

import java.util.Set;

import org.junit.jupiter.api.Test;

import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import ca.gc.aafc.objectstore.api.testsupport.fixtures.ObjectStoreMetadataTestFixture;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectStoreMetadataMapperTest {

  @Test
  public void testMapperToEntity() {
    ObjectStoreMetadataDto dto = ObjectStoreMetadataTestFixture.newObjectStoreMetadata();
    dto.setOriginalFilename("allo.txt");

    ObjectStoreMetadata
      entity = ObjectStoreMetadataMapper.INSTANCE.toEntity(dto, Set.of("originalFilename"), null);

    assertEquals(dto.getOriginalFilename(), entity.getOriginalFilename());
  }

  @Test
  public void testMapperToDto() {

    ObjectStoreMetadata entity = ObjectStoreMetadataFactory.newEmptyObjectStoreMetadata();
    entity.setOriginalFilename("allo.txt");

    ObjectStoreMetadataDto dto = ObjectStoreMetadataMapper.INSTANCE.toDto(entity, Set.of("originalFilename"), null);

    assertEquals(entity.getOriginalFilename(), dto.getOriginalFilename());
  }

}
