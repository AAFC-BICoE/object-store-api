package ca.gc.aafc.objectstore.api.mapper;

import java.util.Set;

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
import ca.gc.aafc.objectstore.api.dto.DerivativeDto;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;

@Mapper
public interface ObjectStoreMetadataMapper
  extends DinaMapperV2<ObjectStoreMetadataDto, ObjectStoreMetadata> {

  ObjectStoreMetadataMapper INSTANCE = Mappers.getMapper(ObjectStoreMetadataMapper.class);

  @Mapping(source = "acMetadataCreator", target = "acMetadataCreator", qualifiedByName = "uuidToPersonExternalRelation")
  @Mapping(source = "dcCreator", target = "dcCreator", qualifiedByName = "uuidToPersonExternalRelation")
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
   * @param dto
   * @param provided
   * @param scope will be ignored but required so MapStruct uses it
   * @return
   */
  default Derivative toEntity(DerivativeDto dto, @Context Set<String> provided, @Context String scope) {
    return toDerivativeEntity(dto, provided, "derivative");
  }

  /**
   * Default method to intercept the mapping and set the context to the relationship
   * @param dto
   * @param provided
   * @param scope will be ignored but required so MapStruct uses it
   * @return
   */
  default DerivativeDto toDto(Derivative dto, @Context Set<String> provided, @Context String scope) {
    return toDerivativeDto(dto, provided, "derivative");
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
