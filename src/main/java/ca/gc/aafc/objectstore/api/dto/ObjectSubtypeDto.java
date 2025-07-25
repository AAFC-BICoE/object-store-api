package ca.gc.aafc.objectstore.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import ca.gc.aafc.dina.dto.RelatedEntity;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.toedter.spring.hateoas.jsonapi.JsonApiId;
import com.toedter.spring.hateoas.jsonapi.JsonApiTypeForClass;

@RelatedEntity(ObjectSubtype.class)
@Data
@JsonApiTypeForClass(ObjectSubtypeDto.TYPENAME)
public class ObjectSubtypeDto implements ca.gc.aafc.dina.dto.JsonApiResource {

  public static final String TYPENAME = "object-subtype";

  @JsonApiId
  private UUID uuid;

  private DcType dcType;
  private String acSubtype;
  private String createdBy;
  private OffsetDateTime createdOn;

  @Override
  @JsonIgnore
  public String getJsonApiType() {
    return TYPENAME;
  }

  @Override
  @JsonIgnore
  public UUID getJsonApiId() {
    return uuid;
  }
}
