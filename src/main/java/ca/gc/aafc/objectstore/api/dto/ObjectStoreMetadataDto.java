package ca.gc.aafc.objectstore.api.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import lombok.Data;

@SuppressFBWarnings({ "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
@RelatedEntity(ObjectStoreMetadata.class)
@Data
@JsonApiResource(type = "metadata")
public class ObjectStoreMetadataDto {
  
  @JsonApiId
  private UUID uuid;
  
  private String bucket;
  private UUID fileIdentifier;
  private String fileExtension;

  private String dcFormat;
  private DcType dcType;
  
  @JsonInclude(Include.NON_EMPTY)
  private String acCaption;

  private OffsetDateTime acDigitizationDate;
  private OffsetDateTime xmpMetadataDate;
  
  private String xmpRightsWebStatement;
  private String dcRights;
  private String xmpRightsOwner;
  
  @JsonInclude(Include.NON_EMPTY)
  private String originalFilename;

  private String acHashFunction;
  private String acHashValue;
  
  private OffsetDateTime createdDate;
  @JsonInclude(Include.NON_EMPTY)
  private OffsetDateTime deletedDate;
  
  @JsonInclude(Include.NON_EMPTY)
  private String[] acTags;
  
  @JsonApiRelation
  private List<MetadataManagedAttributeDto> managedAttribute;

  @JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS)
  private ManagedAttributeMapDto managedAttributeMap;
  
  @JsonApiRelation
  private AgentDto acMetadataCreator;
  
  @JsonApiRelation
  private ObjectStoreMetadataDto acDerivedFrom;

  @JsonApiRelation
  private AgentDto dcCreator;

  private boolean publiclyReleasable;

  @JsonInclude(Include.NON_EMPTY)
  private String notPubliclyReleasableReason;

  @JsonInclude(Include.NON_EMPTY)
  private String acSubType;
}
