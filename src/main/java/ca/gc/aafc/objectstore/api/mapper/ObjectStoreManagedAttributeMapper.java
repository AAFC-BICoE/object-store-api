package ca.gc.aafc.objectstore.api.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import ca.gc.aafc.dina.mapper.DinaMapperV2;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreManagedAttributeDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreManagedAttribute;

import java.util.Set;

@Mapper
public interface ObjectStoreManagedAttributeMapper extends
  DinaMapperV2<ObjectStoreManagedAttributeDto, ObjectStoreManagedAttribute> {

  ObjectStoreManagedAttributeMapper INSTANCE = Mappers.getMapper(ObjectStoreManagedAttributeMapper.class);

  ObjectStoreManagedAttributeDto toDto(ObjectStoreManagedAttribute entity, @Context Set<String> provided, @Context String scope);

  ObjectStoreManagedAttribute toEntity(ObjectStoreManagedAttributeDto dto, @Context Set<String> provided, @Context String scope);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void patchEntity(@MappingTarget ObjectStoreManagedAttribute entity, ObjectStoreManagedAttributeDto dto, @Context Set<String> provided, @Context String scope);

}
