package ca.gc.aafc.objectstore.api.dto;

import java.util.UUID;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.Data;

@Data
@JsonApiResource(type = "object")
public class ObjectStoreMetadataDto {
  
  @JsonApiId
  private UUID uuid;

  private String dcFormat;
  private String dcType;

  private String acDigitizationDate;
  private String xmpMetadataDdate;

  private String acHashFunction;
  private String acHashValue;
  
}
