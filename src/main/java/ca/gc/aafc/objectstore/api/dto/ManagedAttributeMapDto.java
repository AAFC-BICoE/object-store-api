package ca.gc.aafc.objectstore.api.dto;

import java.util.HashMap;
import java.util.Map;

import org.javers.core.metamodel.annotation.DiffIgnore;
import org.javers.core.metamodel.annotation.TypeName;
import org.javers.core.metamodel.annotation.Value;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@SuppressFBWarnings({ "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
@Data
@JsonApiResource(type = ManagedAttributeMapDto.TYPENAME)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TypeName(ManagedAttributeMapDto.TYPENAME)
@Value // This class is considered a "value" belonging to an ObjectStoreMetadataDto.
public class ManagedAttributeMapDto {

  public static final String TYPENAME = "managed-attribute-map";

  /** ID required by Crnk but not by Javers. */
  @DiffIgnore
  @EqualsAndHashCode.Exclude // "equals(...)" comparisons should ignore this generated field.
  @JsonApiId
  private String id;
  
  /**
   * Map of ManagedAttribute UUIDs to values.
   */
  @Builder.Default
  private Map<String, ManagedAttributeMapValue> values = new HashMap<>();
  
  @DiffIgnore
  @EqualsAndHashCode.Exclude // "equals(...)" comparisons should ignore this generated field.
  @JsonApiRelation
  private ObjectStoreMetadataDto metadata;

  @Data
  @Builder
  @Value
  public static class ManagedAttributeMapValue {

    // Don't include the attribute name in audits, because it doesn't "belong" to the ManagedAttributeMap.
    // It can change independently of the Metadata or ManagedAttributeMap.
    @DiffIgnore
    @EqualsAndHashCode.Exclude // "equals(...)" comparisons should ignore this generated field.
    private String name;

    private String value;

  }

}
