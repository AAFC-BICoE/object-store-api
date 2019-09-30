package ca.gc.aafc.objectstore.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;

@Mapper(componentModel = "spring")
public interface ObjectStoreMetadataMapper {
  
  ObjectStoreMetadataDto sourceToDestination(ObjectStoreMetadata source);
  
  @Mapping(target = "id", ignore = true)
  ObjectStoreMetadata destinationToSource(ObjectStoreMetadataDto destination);
}
