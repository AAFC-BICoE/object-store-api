package ca.gc.aafc.objectstore.api.dto;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import org.javers.core.metamodel.annotation.Id;
import org.javers.core.metamodel.annotation.PropertyName;

import ca.gc.aafc.dina.dto.RelatedEntity;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute.ManagedAttributeType;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.Data;

@SuppressFBWarnings({ "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
@RelatedEntity(ManagedAttribute.class)
@Data
@JsonApiResource(type = "managed-attribute")
public class ManagedAttributeDto {

  @JsonApiId
  @Id
  @PropertyName("id")
  private UUID uuid;

  private String name;
  private ManagedAttributeType managedAttributeType;
  private String[] acceptedValues;
  private OffsetDateTime createdDate;
  private OffsetDateTime createdOn;
  private String createdBy;
  private Map<String, String> description;

}
