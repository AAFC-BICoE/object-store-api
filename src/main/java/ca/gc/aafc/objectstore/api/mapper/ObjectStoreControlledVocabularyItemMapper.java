package ca.gc.aafc.objectstore.api.mapper;


import org.mapstruct.BeanMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import ca.gc.aafc.dina.mapper.DinaMapperV2;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreControlledVocabularyItemDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreControlledVocabularyItem;

import java.util.Set;

@Mapper
public interface ObjectStoreControlledVocabularyItemMapper extends DinaMapperV2<ObjectStoreControlledVocabularyItemDto, ObjectStoreControlledVocabularyItem> {

  ObjectStoreControlledVocabularyItemMapper INSTANCE = Mappers.getMapper(ObjectStoreControlledVocabularyItemMapper.class);

  ObjectStoreControlledVocabularyItemDto toDto(ObjectStoreControlledVocabularyItem entity, @Context Set<String> provided, @Context String scope);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "controlledVocabulary", ignore = true)
  ObjectStoreControlledVocabularyItem toEntity(ObjectStoreControlledVocabularyItemDto dto, @Context Set<String> provided, @Context String scope);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "controlledVocabulary", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void patchEntity(@MappingTarget ObjectStoreControlledVocabularyItem entity, ObjectStoreControlledVocabularyItemDto dto, @Context Set<String> provided, @Context String scope);
}
