package ca.gc.aafc.objectstore.api.dto;

import org.javers.core.metamodel.annotation.Id;
import org.javers.core.metamodel.annotation.PropertyName;
import org.javers.core.metamodel.annotation.TypeName;

import com.toedter.spring.hateoas.jsonapi.JsonApiId;
import com.toedter.spring.hateoas.jsonapi.JsonApiTypeForClass;

import ca.gc.aafc.dina.dto.BaseControlledVocabularyDto;
import ca.gc.aafc.dina.dto.RelatedEntity;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreControlledVocabulary;

import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@RelatedEntity(ObjectStoreControlledVocabulary.class)
@JsonApiTypeForClass(BaseControlledVocabularyDto.TYPENAME)
@Data
@TypeName(BaseControlledVocabularyDto.TYPENAME)
public class ObjectStoreControlledVocabularyDto extends BaseControlledVocabularyDto {

  @JsonApiId
  @Id
  @PropertyName("id")
  public UUID getUuid() {
    return uuid;
  }
}
