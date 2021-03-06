package ca.gc.aafc.objectstore.api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.repository.DtoEntityMapping;

public class DtoEntityMappingTest {

  @Test
  public void entityMapping_onValidDto_ReturnMatchingEntity() {
    // Note: ObjectStoreMetadataDto is used to demonstrate that the specific class
    // is irrelevant. It
    // is only used to get the package name.
    assertEquals(
      ObjectStoreMetadata.class,
      DtoEntityMapping
        .getDtoToEntityMapping(ObjectStoreMetadataDto.class)
        .get(ObjectStoreMetadataDto.class)
    );
  }

}
