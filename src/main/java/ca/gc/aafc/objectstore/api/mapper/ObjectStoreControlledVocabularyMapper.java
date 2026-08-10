package ca.gc.aafc.objectstore.api.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import ca.gc.aafc.dina.mapper.DinaMapperV2;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreControlledVocabularyDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreControlledVocabulary;

import java.util.Set;

@Mapper
public interface ObjectStoreControlledVocabularyMapper extends DinaMapperV2<ObjectStoreControlledVocabularyDto, ObjectStoreControlledVocabulary> {

  ObjectStoreControlledVocabularyMapper INSTANCE = Mappers.getMapper(ObjectStoreControlledVocabularyMapper.class);

  ObjectStoreControlledVocabularyDto toDto(ObjectStoreControlledVocabulary entity, @Context Set<String> provided, @Context String scope);

  @Mapping(target = "id", ignore = true)
  ObjectStoreControlledVocabulary toEntity(ObjectStoreControlledVocabularyDto dto, @Context Set<String> provided, @Context String scope);

  @Mapping(target = "id", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void patchEntity(@MappingTarget ObjectStoreControlledVocabulary entity, ObjectStoreControlledVocabularyDto dto, @Context Set<String> provided, @Context String scope);
}
