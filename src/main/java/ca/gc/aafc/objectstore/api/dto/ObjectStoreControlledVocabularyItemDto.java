package ca.gc.aafc.objectstore.api.dto;

import org.javers.core.metamodel.annotation.Id;
import org.javers.core.metamodel.annotation.PropertyName;
import org.javers.core.metamodel.annotation.ShallowReference;
import org.javers.core.metamodel.annotation.TypeName;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.toedter.spring.hateoas.jsonapi.JsonApiId;
import com.toedter.spring.hateoas.jsonapi.JsonApiTypeForClass;

import ca.gc.aafc.dina.dto.BaseControlledVocabularyItemDto;
import ca.gc.aafc.dina.dto.RelatedEntity;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreControlledVocabularyItem;

import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@RelatedEntity(ObjectStoreControlledVocabularyItem.class)
@JsonApiTypeForClass(BaseControlledVocabularyItemDto.TYPENAME)
@Data
@TypeName(BaseControlledVocabularyItemDto.TYPENAME)
public class ObjectStoreControlledVocabularyItemDto extends BaseControlledVocabularyItemDto<ObjectStoreControlledVocabularyDto> {

  private ObjectStoreControlledVocabularyDto controlledVocabulary;

  @JsonApiId
  @Id
  @PropertyName("id")
  public UUID getUuid() {
    return uuid;
  }

  @Override
  @JsonIgnore
  @ShallowReference
  public ObjectStoreControlledVocabularyDto getControlledVocabulary() {
    return controlledVocabulary;
  }
}
