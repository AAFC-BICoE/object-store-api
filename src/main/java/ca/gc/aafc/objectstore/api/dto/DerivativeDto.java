package ca.gc.aafc.objectstore.api.dto;

import ca.gc.aafc.dina.dto.RelatedEntity;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
@RelatedEntity(Derivative.class)
@Data
@Builder
@JsonApiResource(type = DerivativeDto.TYPENAME)
public class DerivativeDto {

  public static final String TYPENAME = "derivative";

  @JsonApiId
  private UUID uuid;
  private String bucket;
  private UUID fileIdentifier;
  private String fileExtension;
  private DcType dcType;
  private String acHashFunction;
  private String acHashValue;
  private String createdBy;
  private OffsetDateTime createdOn;

  @JsonApiRelation
  private ObjectSubtypeDto objectSubtype;
  @JsonApiRelation
  private ObjectStoreMetadata acDerivedFrom;
}
