package ca.gc.aafc.objectstore.api.dto;

import java.util.HashMap;
import java.util.Map;

import ca.gc.aafc.dina.dto.RelatedEntity;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@SuppressFBWarnings({ "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
@Data
@JsonApiResource(type = "managed-attribute-map")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RelatedEntity(ManagedAttributeMapDto.class) // Needed for Dina Mapper
public class ManagedAttributeMapDto {

  @JsonApiId
  private String id;

  /**
   * Map of ManagedAttribute UUIDs to values.
   */
  @Builder.Default
  private Map<String, ManagedAttributeMapValue> values = new HashMap<>();

  @JsonApiRelation
  private ObjectStoreMetadataDto metadata;

  @Data
  @Builder
  public static class ManagedAttributeMapValue {
    private String name;
    private String value;
  }

}
