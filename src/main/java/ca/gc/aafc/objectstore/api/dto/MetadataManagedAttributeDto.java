package ca.gc.aafc.objectstore.api.dto;

import ca.gc.aafc.dina.dto.RelatedEntity;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.Data;
import org.javers.core.metamodel.annotation.PropertyName;

import java.time.OffsetDateTime;
import java.util.UUID;

@SuppressFBWarnings({ "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
@RelatedEntity(MetadataManagedAttribute.class)
@Data
@JsonApiResource(type = "metadata-managed-attribute")
public class MetadataManagedAttributeDto {

  @JsonApiId
  @PropertyName("id")
  private UUID uuid;

  private String assignedValue;
  private String createdBy;
  private OffsetDateTime createdOn;

  @JsonApiRelation
  private ObjectStoreMetadataDto objectStoreMetadata;

  @JsonApiRelation
  private ManagedAttributeDto managedAttribute;
}
