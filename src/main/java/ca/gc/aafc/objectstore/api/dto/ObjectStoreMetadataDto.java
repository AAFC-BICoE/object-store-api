package ca.gc.aafc.objectstore.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata.DcType;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@JsonApiResource(type = "objectStoreMetadataDto")
public class ObjectStoreMetadataDto {
  
  @JsonApiId
  private Integer id;
  private UUID uuid;

  private String dcFormat;
  private DcType dcType;

  private OffsetDateTime acDigitizationDate;
  private OffsetDateTime xmpMetadataDdate;

  private String acHashFunction;
  private String acHashValue;
  
}
