package ca.gc.aafc.objectstore.api.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ca.gc.aafc.objectstore.api.entities.ManagedAttribute.ManagedAttributeType;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.Data;

@SuppressFBWarnings({ "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
@Data
@JsonApiResource(type = "managed-attribute")
public class ManagedAttributeDto {
  
  @JsonApiId 
  private UUID uuid;
  
  private String name;
  private ManagedAttributeType managedAttributeType;
  private List<String> acceptedValues;
  private OffsetDateTime createdDate;
  
  private Map<String, String> description;
  
}
