package ca.gc.aafc.objectstore.api.mapper;

import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;

public interface ObjectStoreMetadataMapper {
  
  ObjectStoreMetadataMapper INSTANCE = Mappers.getMapper(ObjectStoreMetadataMapper.class);  
  
  ObjectStoreMetadataDto objectStoreMetadataToObjectStoreMetadataDto(ObjectStoreMetadata entity);
  
  @Mapping(target = "id", ignore = true)
  ObjectStoreMetadata objectStoreMetadataDtotoObjectStoreMetadata(ObjectStoreMetadataDto dto);
    
}
