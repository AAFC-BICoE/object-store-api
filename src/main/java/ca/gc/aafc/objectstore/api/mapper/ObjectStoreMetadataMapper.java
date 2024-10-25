package ca.gc.aafc.objectstore.api.mapper;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import ca.gc.aafc.dina.mapper.DinaMapperV2;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;

@Mapper
public interface ObjectStoreMetadataMapper
  extends DinaMapperV2<ObjectStoreMetadataDto, ObjectStoreMetadata> {

  ObjectStoreMetadataMapper INSTANCE = Mappers.getMapper(ObjectStoreMetadataMapper.class);

  @Mapping(source = "acMetadataCreator", target = "acMetadataCreator", qualifiedByName = "uuidToPersonExternalRelation")
  @Mapping(source = "dcCreator", target = "dcCreator", qualifiedByName = "uuidToPersonExternalRelation")
  ObjectStoreMetadataDto toDto(ObjectStoreMetadata entity, @Context Set<String> provided);

  @Mapping(target = "acSubtype", ignore = true)
  ObjectStoreMetadata toEntity(ObjectStoreMetadataDto dto, @Context Set<String> provided);

  default String objectSubtypeToString(ObjectSubtype objectSubtype) {
    if (objectSubtype != null &&
      objectSubtype.getDcType() != null &&
      StringUtils.isNotBlank(objectSubtype.getAcSubtype())) {
      return objectSubtype.getAcSubtype();
    }
    return null;
  }


  @AfterMapping
  default void afterObjectStoreMetadataMapping(@MappingTarget ObjectStoreMetadata entity,
                               ObjectStoreMetadataDto dto) {
    if (dto.getDcType() == null || StringUtils.isBlank(dto.getAcSubtype())) {
      entity.setAcSubtype(null);
    }
    entity.setAcSubtype(ObjectSubtype.builder()
      .dcType(dto.getDcType())
      .acSubtype(dto.getAcSubtype())
      .build());
  }

}
