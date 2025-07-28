package ca.gc.aafc.objectstore.api.mapper;

import java.util.Set;

import org.mapstruct.BeanMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import ca.gc.aafc.dina.mapper.DinaMapperV2;
import ca.gc.aafc.objectstore.api.dto.ObjectUploadDto;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;

@Mapper
public interface ObjectUploadMapper extends DinaMapperV2<ObjectUploadDto, ObjectUpload> {

  ObjectUploadMapper INSTANCE = Mappers.getMapper(ObjectUploadMapper.class);

  ObjectUploadDto toDto(ObjectUpload entity, @Context Set<String> provided, @Context String scope);

  ObjectUpload toEntity(ObjectUploadDto dto, @Context Set<String> provided, @Context String scope);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void patchEntity(@MappingTarget ObjectUpload entity, ObjectUploadDto dto,
                   @Context Set<String> provided, @Context String scope);
}
