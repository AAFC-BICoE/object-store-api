package ca.gc.aafc.objectstore.api.mapper;

import java.util.List;
import java.util.function.Function;

import org.mapstruct.Context;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import ca.gc.aafc.objectstore.api.dto.MetadataManagedAttributeDto;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;

@Mapper(componentModel = "spring")
public interface ObjectStoreMetadataMapper {

  ObjectStoreMetadataMapper INSTANCE = Mappers.getMapper(ObjectStoreMetadataMapper.class);

  @Mapping(target = "managedAttribute", qualifiedByName="metadataManagedAttributeIdOnly")
  ObjectStoreMetadataDto toDto(ObjectStoreMetadata entity, @Context Function<String, Boolean> isAvailable, @Context CycleAvoidingMappingContext context);
  

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "managedAttribute", ignore = true)
  @Mapping(target = "acMetadataCreator", ignore = true)
  @Mapping(target = "dcCreator", ignore = true)
  ObjectStoreMetadata toEntity(ObjectStoreMetadataDto dto);
  
  @InheritConfiguration
  void updateObjectStoreMetadataFromDto(ObjectStoreMetadataDto dto, @MappingTarget ObjectStoreMetadata entity);
   
  /**
   * Used to avoid cyclic reference since managedAttribute points back to ObjectStoreMetadata.
   * @param mma list to transform
   * @param isAvailable function to check if a specific attribute is available (can be loaded without lazy-loading)
   * @param context mapping context used to avoid cyclic references
   * @return
   */
  @Named("metadataManagedAttributeIdOnly")
  default List<MetadataManagedAttributeDto> metadataManagedAttributeListToMetadataManagedAttributeDtoList(List<MetadataManagedAttribute> mma, @Context Function<String, Boolean> isAvailable, 
      @Context CycleAvoidingMappingContext context) {
    if (mma == null || isAvailable == null || !isAvailable.apply("managedAttribute")) {
      return null;
    }
    return MetadataManagedAttributeMapper.INSTANCE.toDtoList(mma, context);
  }

}
