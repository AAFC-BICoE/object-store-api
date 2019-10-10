package ca.gc.aafc.objectstore.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import ca.gc.aafc.objectstore.api.dto.ManagedAttributeDto;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;

@Mapper(componentModel = "spring")
public interface ManagedAttributeMapper {
  
  ManagedAttributeDto toDto(ManagedAttribute entity);
  ManagedAttribute toEntity(ManagedAttributeDto dto);
  
  void updateManagedAttributeFromDto(ManagedAttributeDto dto, @MappingTarget ManagedAttribute entity);
  
}
