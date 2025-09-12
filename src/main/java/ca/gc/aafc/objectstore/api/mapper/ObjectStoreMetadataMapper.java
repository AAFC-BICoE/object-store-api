package ca.gc.aafc.objectstore.api.mapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import ca.gc.aafc.dina.mapper.DinaMapperV2;
import ca.gc.aafc.dina.mapper.MapperStaticConverter;
import ca.gc.aafc.objectstore.api.dto.DerivativeDto;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;

@Mapper(imports = MapperStaticConverter.class)
public interface ObjectStoreMetadataMapper
  extends DinaMapperV2<ObjectStoreMetadataDto, ObjectStoreMetadata> {

  ObjectStoreMetadataMapper INSTANCE = Mappers.getMapper(ObjectStoreMetadataMapper.class);

  @Mapping(target = "acMetadataCreator", expression = "java(MapperStaticConverter.uuidToExternalRelation(entity.getAcMetadataCreator(), \"person\"))")
  @Mapping(target = "dcCreator", expression = "java(MapperStaticConverter.uuidToExternalRelation(entity.getDcCreator(), \"person\"))")
  ObjectStoreMetadataDto toDto(ObjectStoreMetadata entity, @Context Set<String> provided, @Context String scope);

  /**
   * Ignore internal id
   * Ignore special fields like acSubtype and acSubtypeId.
   * Ignore relationships for MapStruct mapping dto -> entity
   * @param dto
   * @param provided provided properties so only those will be set
   * @param scope used to check provided properties within nested properties
   * @return
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "acSubtype", ignore = true)
  @Mapping(target = "acSubtypeId", ignore = true)
  @Mapping(target = "acMetadataCreator", ignore = true)
  @Mapping(target = "dcCreator", ignore = true)
  @Mapping(target = "derivatives", ignore = true)
  ObjectStoreMetadata toEntity(ObjectStoreMetadataDto dto, @Context Set<String> provided, @Context String scope);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "acSubtype", ignore = true)
  @Mapping(target = "acSubtypeId", ignore = true)
  @Mapping(target = "acMetadataCreator", ignore = true)
  @Mapping(target = "dcCreator", ignore = true)
  @Mapping(target = "derivatives", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void patchEntity(@MappingTarget ObjectStoreMetadata entity, ObjectStoreMetadataDto dto, @Context Set<String> provided, @Context String scope);

  /**
   * Default method to intercept the mapping and set the context to the relationship
   * @param entities
   * @param provided
   * @param scope will be ignored but required so MapStruct uses it
   * @return
   */
  default List<DerivativeDto> toDto(List<Derivative> entities, @Context Set<String> provided, @Context String scope) {
    if(CollectionUtils.isEmpty(entities)) {
      return null;
    }
    return entities.stream().map(d -> toDerivativeDto(d, provided, "derivatives"))
      .collect(Collectors.toList());
  }

  // Specific type mapping
  default String objectSubtypeToString(ObjectSubtype objectSubtype) {
    if (objectSubtype != null &&
      objectSubtype.getDcType() != null &&
      StringUtils.isNotBlank(objectSubtype.getAcSubtype())) {
      return objectSubtype.getAcSubtype();
    }
    return null;
  }

  // Relationships handling
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "acDerivedFrom", ignore = true)
  @Mapping(target = "generatedFromDerivative", ignore = true)
  Derivative toDerivativeEntity(DerivativeDto dto, Set<String> provided, String scope);

  @Mapping(target = "acDerivedFrom", ignore = true)
  @Mapping(target = "generatedFromDerivative", ignore = true)
  DerivativeDto toDerivativeDto(Derivative entity, Set<String> provided, String scope);

  // After mapping customization

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
