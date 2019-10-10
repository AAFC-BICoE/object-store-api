package ca.gc.aafc.objectstore.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import ca.gc.aafc.objectstore.api.dto.ManagedAttributeDto;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;

@Mapper(componentModel = "spring")
public interface ManagedAttributeMapper {
  ManagedAttributeMapper INSTANCE = Mappers.getMapper(ManagedAttributeMapper.class);  
  
  ManagedAttributeDto toDto(ManagedAttribute entity);
  ManagedAttribute toEntity(ManagedAttributeDto dto);
  
  void updateManagedAttributeFromDto(ManagedAttributeDto dto, @MappingTarget ManagedAttribute entity);
  
}
