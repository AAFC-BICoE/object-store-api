package ca.gc.aafc.objectstore.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;

@Mapper(componentModel = "spring")
public interface ObjectStoreMetadataMapper {

  ObjectStoreMetadataMapper INSTANCE = Mappers.getMapper(ObjectStoreMetadataMapper.class);

  ObjectStoreMetadataDto toDto(ObjectStoreMetadata entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "managedAttributes", ignore = true)
  ObjectStoreMetadata toEntity(ObjectStoreMetadataDto dto);
  
  @Mapping(target = "managedAttributes", ignore = true)
  void updateObjectStoreMetadataFromDto(ObjectStoreMetadataDto dto, @MappingTarget ObjectStoreMetadata entity);  

}
