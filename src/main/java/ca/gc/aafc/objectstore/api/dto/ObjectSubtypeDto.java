package ca.gc.aafc.objectstore.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import ca.gc.aafc.dina.dto.RelatedEntity;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.Data;

@RelatedEntity(ObjectSubtype.class)
@Data
@JsonApiResource(type = "object-subtype")
public class ObjectSubtypeDto {
  
  @JsonApiId 
  private UUID uuid;

  private DcType dcType;
  private String acSubtype;
  private String createdBy;
  private OffsetDateTime createdOn;
}
