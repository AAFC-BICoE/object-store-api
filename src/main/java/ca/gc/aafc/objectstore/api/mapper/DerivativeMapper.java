package ca.gc.aafc.objectstore.api.mapper;

import java.util.Set;

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

@Mapper
public interface DerivativeMapper extends DinaMapperV2<DerivativeDto, Derivative> {

  DerivativeMapper INSTANCE = Mappers.getMapper(DerivativeMapper.class);

  DerivativeDto toDto(Derivative entity, @Context Set<String> provided, @Context String scope);

  Derivative toEntity(DerivativeDto dto, @Context Set<String> provided, @Context String scope);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "acDerivedFrom", ignore = true)
  @Mapping(target = "generatedFromDerivative", ignore = true)
  void patchEntity(@MappingTarget Derivative entity, DerivativeDto dto, @Context Set<String> provided, @Context String scope);

  /**
   * Default method to intercept the mapping and set the context to the relationship
   * @param dto
   * @param provided
   * @param scope will be ignored but required so MapStruct uses it
   * @return
   */
  default ObjectStoreMetadata toEntity(ObjectStoreMetadataDto dto, @Context Set<String> provided, @Context String scope) {
    return toMetadataEntity(dto, provided, "metadata");
  }

  /**
   * Default method to intercept the mapping and set the context to the relationship
   * @param entity
   * @param provided
   * @param scope will be ignored but required so MapStruct uses it
   * @return
   */
  default ObjectStoreMetadataDto toDto(ObjectStoreMetadata entity, @Context Set<String> provided, @Context String scope) {
    return toMetadataDto(entity, provided, "metadata");
  }

  // Relationships handling

  @Mapping(target = "derivatives", ignore = true)
  @Mapping(target = "acSubtype", ignore = true)
  @Mapping(target = "acMetadataCreator", ignore = true)
  @Mapping(target = "dcCreator", ignore = true)
  ObjectStoreMetadata toMetadataEntity(ObjectStoreMetadataDto dto, Set<String> provided, String scope);

  @Mapping(target = "derivatives", ignore = true)
  @Mapping(target = "acSubtype", ignore = true)
  @Mapping(target = "dcCreator", qualifiedByName = "uuidToPersonExternalRelation")
  @Mapping(target = "acMetadataCreator", qualifiedByName = "uuidToPersonExternalRelation")
  ObjectStoreMetadataDto toMetadataDto(ObjectStoreMetadata entity, Set<String> provided, String scope);
}
