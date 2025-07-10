package ca.gc.aafc.objectstore.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import ca.gc.aafc.dina.vocabulary.TypedVocabularyElement;
import org.javers.core.metamodel.annotation.Id;
import org.javers.core.metamodel.annotation.PropertyName;

import ca.gc.aafc.dina.dto.RelatedEntity;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreManagedAttribute;
import ca.gc.aafc.dina.i18n.MultilingualDescription;
import io.crnk.core.resource.annotations.JsonApiField;
import lombok.Data;

import com.toedter.spring.hateoas.jsonapi.JsonApiId;
import com.toedter.spring.hateoas.jsonapi.JsonApiTypeForClass;

@RelatedEntity(ObjectStoreManagedAttribute.class)
@Data
@JsonApiTypeForClass(ObjectStoreManagedAttributeDto.TYPENAME)
public class ObjectStoreManagedAttributeDto implements ca.gc.aafc.dina.dto.JsonApiResource {

  public static final String TYPENAME = "managed-attribute";

  @JsonApiId
  @Id
  @PropertyName("id")
  private UUID uuid;

  @JsonApiField(patchable = false)
  private String name;
  
  @JsonApiField(patchable = false)
  private String key;

  private TypedVocabularyElement.VocabularyElementType vocabularyElementType;
  private String[] acceptedValues;
  private OffsetDateTime createdOn;
  private String createdBy;
  private MultilingualDescription multilingualDescription;

  @Override
  public String getJsonApiType() {
    return TYPENAME;
  }

  @Override
  public UUID getJsonApiId() {
    return uuid;
  }
}
