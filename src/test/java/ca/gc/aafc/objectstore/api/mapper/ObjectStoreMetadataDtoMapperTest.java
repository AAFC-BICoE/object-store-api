package ca.gc.aafc.objectstore.api.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata.DcType;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;

public class ObjectStoreMetadataDtoMapperTest {

  private static final ZoneId MTL_TZ = ZoneId.of("America/Montreal");
  private final ZonedDateTime TEST_ZONED_DT = ZonedDateTime.of(2019, 1, 2, 3, 4, 5, 0, MTL_TZ);
  private final OffsetDateTime TEST_OFFSET_DT = TEST_ZONED_DT.toOffsetDateTime();

  private static final ObjectStoreMetadataMapper DTO_MAPPER = Mappers
      .getMapper(ObjectStoreMetadataMapper.class);

  @Test
  public void testGivenObjectStoreMetadata_mapsToObjectStoreMetadataDto() {

    // given
    ObjectStoreMetadata objectStoreMetadata = ObjectStoreMetadataFactory.newObjectStoreMetadata()
        .acDigitizationDate(TEST_OFFSET_DT).build();

    // when
    ObjectStoreMetadataDto objectStoreMetadataDto = DTO_MAPPER
        .sourceToDestination(objectStoreMetadata);

    // then
    assertEquals(objectStoreMetadataDto.getAcDigitizationDate(),
        objectStoreMetadata.getAcDigitizationDate());
    assertEquals(objectStoreMetadataDto.getUuid(), objectStoreMetadata.getUuid());
    assertEquals(objectStoreMetadataDto.getDcType(), objectStoreMetadata.getDcType());
  }

  @Test
  public void testGivenObjectStoreMetadataDto_mapsToObjectStoreMetadata() {

    // given
    ObjectStoreMetadataDto objectStoreMetadataDto = new ObjectStoreMetadataDto();
    objectStoreMetadataDto.setUuid(UUID.randomUUID());
    objectStoreMetadataDto.setDcType(DcType.IMAGE);
    objectStoreMetadataDto.setAcDigitizationDate(TEST_OFFSET_DT);

    // when
    ObjectStoreMetadata objectStoreMetadata = DTO_MAPPER
        .destinationToSource(objectStoreMetadataDto);

    // then
    assertEquals(objectStoreMetadata.getAcDigitizationDate(),
        objectStoreMetadataDto.getAcDigitizationDate());
    assertEquals(objectStoreMetadata.getUuid(), objectStoreMetadataDto.getUuid());
    assertEquals(objectStoreMetadata.getDcType(), objectStoreMetadataDto.getDcType());
  }

}
