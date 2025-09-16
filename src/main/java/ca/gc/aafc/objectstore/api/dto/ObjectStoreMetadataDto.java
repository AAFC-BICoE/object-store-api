package ca.gc.aafc.objectstore.api.dto;

import org.javers.core.metamodel.annotation.DiffIgnore;
import org.javers.core.metamodel.annotation.Id;
import org.javers.core.metamodel.annotation.PropertyName;
import org.javers.core.metamodel.annotation.ShallowReference;
import org.javers.core.metamodel.annotation.TypeName;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.toedter.spring.hateoas.jsonapi.JsonApiId;
import com.toedter.spring.hateoas.jsonapi.JsonApiTypeForClass;

import ca.gc.aafc.dina.dto.ExternalRelationDto;
import ca.gc.aafc.dina.dto.JsonApiResource;
import ca.gc.aafc.dina.dto.RelatedEntity;
import ca.gc.aafc.dina.repository.meta.JsonApiExternalRelation;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@RelatedEntity(ObjectStoreMetadata.class)
@Data
@TypeName(ObjectStoreMetadataDto.TYPENAME)
@JsonApiTypeForClass(ObjectStoreMetadataDto.TYPENAME)
public class ObjectStoreMetadataDto implements JsonApiResource {

  public static final String TYPENAME = "metadata";

  @JsonApiId
  @Id
  @PropertyName("id")
  private UUID uuid;

  private String createdBy;
  private OffsetDateTime createdOn;
  private String bucket;
  private UUID fileIdentifier;

  private String originalFilename;

  private String filename;

  private String fileExtension;
  private String resourceExternalURL;

  private String dcFormat;

  @ShallowReference
  private DcType dcType;

  @JsonInclude(Include.NON_EMPTY)
  private String acCaption;

  private OffsetDateTime acDigitizationDate;

  @DiffIgnore
  private OffsetDateTime xmpMetadataDate;

  private String xmpRightsWebStatement;
  private String dcRights;
  private String xmpRightsOwner;
  private String xmpRightsUsageTerms;
  private Integer orientation;

  private String acHashFunction;
  private String acHashValue;

  @JsonInclude(Include.NON_EMPTY)
  private String[] acTags;

  @JsonApiExternalRelation(type = "person")
  @JsonIgnore
  private ExternalRelationDto acMetadataCreator;

  @JsonIgnore
  @DiffIgnore
  private List<DerivativeDto> derivatives = List.of();

  @JsonApiExternalRelation(type = "person")
  @JsonIgnore
  private ExternalRelationDto dcCreator;

  private Boolean publiclyReleasable;

  @JsonInclude(Include.NON_EMPTY)
  private String notPubliclyReleasableReason;

  @JsonInclude(Include.NON_EMPTY)
  private String acSubtype;

  @Setter(AccessLevel.NONE)
  private String group;

  private Map<String, String> managedAttributes = Map.of();

  public String getGroup() {
    return bucket;
  }

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
