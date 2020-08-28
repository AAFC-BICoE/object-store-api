package ca.gc.aafc.objectstore.api.dto;

import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

class ObjectStoreMetadataDtoTest {

  @Test
  void acSubTypeToDTO_ValidInput_acSubTypeReturned() {
    ObjectSubtype objectSubtype = getObjectSubtype(RandomStringUtils.randomAlphabetic(3));
    ObjectStoreMetadata metadata = ObjectStoreMetadata.builder().acSubType(objectSubtype).build();
    Assertions.assertEquals(
      objectSubtype.getAcSubtype(),
      ObjectStoreMetadataDto.acSubTypeToDTO(metadata));
  }

  @Test
  void acSubTypeToDTO_NullAcSubType_NullReturned() {
    ObjectStoreMetadata metadata = ObjectStoreMetadata.builder()
      .acSubType(null)
      .build();
    Assertions.assertNull(ObjectStoreMetadataDto.acSubTypeToDTO(metadata));
  }

  @Test
  void acSubTypeToEntity_ValidInput_acSubTypeReturnedUpperCased() {
    ObjectStoreMetadataDto dto = getObjectStoreMetadataDto(
      DcType.IMAGE,
      RandomStringUtils.randomAlphabetic(3));

    ObjectSubtype result = ObjectStoreMetadataDto.acSubTypeToEntity(dto);
    Assertions.assertNotNull(result);
    Assertions.assertEquals(dto.getDcType(), result.getDcType());
    Assertions.assertEquals(dto.getAcSubType().toUpperCase(), result.getAcSubtype());
  }

  @Test
  void acSubTypeToEntity_NullDcType_NullReturned() {
    ObjectStoreMetadataDto dto = getObjectStoreMetadataDto(
      null,
      RandomStringUtils.randomAlphabetic(3));

    ObjectSubtype result = ObjectStoreMetadataDto.acSubTypeToEntity(dto);
    Assertions.assertNull(result);
  }

  @ParameterizedTest
  @NullAndEmptySource
  void acSubTypeToEntity_BlankAcSubType_NullReturned(String ac) {
    ObjectStoreMetadataDto dto = getObjectStoreMetadataDto(DcType.IMAGE, ac);
    ObjectSubtype result = ObjectStoreMetadataDto.acSubTypeToEntity(dto);
    Assertions.assertNull(result);
  }

  private static ObjectStoreMetadataDto getObjectStoreMetadataDto(DcType dcType, String acSubType) {
    ObjectStoreMetadataDto dto = new ObjectStoreMetadataDto();
    dto.setDcType(dcType);
    dto.setAcSubType(acSubType);
    return dto;
  }

  private static ObjectSubtype getObjectSubtype(String acSubtype) {
    return ObjectSubtype.builder()
      .dcType(DcType.IMAGE)
      .acSubtype(acSubtype)
      .build();
  }
}
