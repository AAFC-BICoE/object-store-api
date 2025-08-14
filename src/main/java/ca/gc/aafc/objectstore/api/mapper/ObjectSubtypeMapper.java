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
import ca.gc.aafc.objectstore.api.dto.ObjectSubtypeDto;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;

@Mapper
public interface ObjectSubtypeMapper extends DinaMapperV2<ObjectSubtypeDto, ObjectSubtype> {

  ObjectSubtypeMapper INSTANCE = Mappers.getMapper(ObjectSubtypeMapper.class);

  ObjectSubtypeDto toDto(ObjectSubtype entity, @Context Set<String> provided, @Context String scope);

  @Mapping(target = "id", ignore = true)
  ObjectSubtype toEntity(ObjectSubtypeDto dto, @Context Set<String> provided, @Context String scope);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  void patchEntity(@MappingTarget ObjectSubtype entity, ObjectSubtypeDto dto, @Context Set<String> provided, @Context String scope);

}
