package ca.gc.aafc.objectstore.api.mapper;

import org.mapstruct.Mapper;

import ca.gc.aafc.objectstore.api.dto.ManagedAttributeDto;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;

@Mapper(componentModel = "spring")
public interface ManagedAttributeMapper {
  ManagedAttributeDto managedAttributeToManagedAttributeDto(ManagedAttribute source);
  ManagedAttribute managedAttributeDtoToManagedAttribute(ManagedAttributeDto destination);
}
