package ca.gc.aafc.objectstore.api.dto;

import ca.gc.aafc.dina.dto.JsonApiResource;
import ca.gc.aafc.dina.dto.RelatedEntity;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.toedter.spring.hateoas.jsonapi.JsonApiId;
import com.toedter.spring.hateoas.jsonapi.JsonApiTypeForClass;

@RelatedEntity(Derivative.class)
@Data
@JsonApiTypeForClass(DerivativeDto.TYPENAME)
public class DerivativeDto implements JsonApiResource {

  public static final String TYPENAME = "derivative";

  @JsonApiId
  private UUID uuid;

  private String bucket;
  private String filename;
  private UUID fileIdentifier;
  private String fileExtension;

  private DcType dcType;
  private String dcFormat;
  private String acHashFunction;
  private String acHashValue;
  private String createdBy;
  private OffsetDateTime createdOn;
  private Derivative.DerivativeType derivativeType;
  private Boolean publiclyReleasable;
  @JsonInclude(Include.NON_EMPTY)
  private String notPubliclyReleasableReason;

  @JsonInclude(Include.NON_EMPTY)
  private String[] acTags;

  // relationships ----
  @JsonIgnore
  private DerivativeDto generatedFromDerivative;

  @JsonIgnore
  private ObjectStoreMetadataDto acDerivedFrom;

  // ----

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
