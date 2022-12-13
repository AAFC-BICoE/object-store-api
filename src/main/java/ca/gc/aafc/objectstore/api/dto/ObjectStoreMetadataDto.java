package ca.gc.aafc.objectstore.api.dto;

import ca.gc.aafc.dina.dto.ExternalRelationDto;
import ca.gc.aafc.dina.dto.RelatedEntity;
import ca.gc.aafc.dina.mapper.CustomFieldAdapter;
import ca.gc.aafc.dina.mapper.DinaFieldAdapter;
import ca.gc.aafc.dina.mapper.IgnoreDinaMapping;
import ca.gc.aafc.dina.repository.meta.AttributeMetaInfoProvider;
import ca.gc.aafc.dina.repository.meta.JsonApiExternalRelation;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.crnk.core.resource.annotations.JsonApiField;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.PatchStrategy;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.javers.core.metamodel.annotation.DiffIgnore;
import org.javers.core.metamodel.annotation.Id;
import org.javers.core.metamodel.annotation.PropertyName;
import org.javers.core.metamodel.annotation.ShallowReference;
import org.javers.core.metamodel.annotation.TypeName;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

@RelatedEntity(ObjectStoreMetadata.class)
@Data
@JsonApiResource(type = ObjectStoreMetadataDto.TYPENAME)
@TypeName(ObjectStoreMetadataDto.TYPENAME)
@CustomFieldAdapter(adapters = ObjectStoreMetadataDto.AcSubtypeAdapter.class)
public class ObjectStoreMetadataDto extends AttributeMetaInfoProvider {

  public static final String TYPENAME = "metadata";

  @JsonApiId
  @Id
  @PropertyName("id")
  private UUID uuid;
  private String createdBy;
  private OffsetDateTime createdOn;
  private String bucket;
  private UUID fileIdentifier;
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

  @JsonInclude(Include.NON_EMPTY)
  private String originalFilename;

  private String acHashFunction;
  private String acHashValue;

  @JsonInclude(Include.NON_EMPTY)
  private String[] acTags;

  @JsonApiExternalRelation(type = "person")
  @JsonApiRelation
  private ExternalRelationDto acMetadataCreator;

  @JsonApiRelation
  @DiffIgnore
  private List<DerivativeDto> derivatives = List.of();

  @JsonApiExternalRelation(type = "person")
  @JsonApiRelation
  private ExternalRelationDto dcCreator;

  private Boolean publiclyReleasable;

  @JsonInclude(Include.NON_EMPTY)
  private String notPubliclyReleasableReason;

  @JsonInclude(Include.NON_EMPTY)
  @IgnoreDinaMapping(reason = "Custom Resolved field")
  private String acSubtype;

  @Setter(AccessLevel.NONE)
  private String group;

  @JsonApiField(patchStrategy = PatchStrategy.SET)
  private Map<String, String> managedAttributes = Map.of();

  public String getGroup() {
    return bucket;
  }

  public void applyObjectSubtype(ObjectSubtype objectSubtype) {
    if (objectSubtype != null &&
        objectSubtype.getDcType() != null &&
        StringUtils.isNotBlank(objectSubtype.getAcSubtype())) {
      setAcSubtype(objectSubtype.getAcSubtype());
    } else {
      setAcSubtype(null);
    }
  }
  public ObjectSubtype supplyObjectSubtype() {
    if (getDcType() == null || StringUtils.isBlank(getAcSubtype())) {
      return null;
    }
    return ObjectSubtype.builder()
        .dcType(getDcType())
        .acSubtype(getAcSubtype())
        .build();
  }

  public static class AcSubtypeAdapter
    implements DinaFieldAdapter<ObjectStoreMetadataDto, ObjectStoreMetadata, ObjectSubtype, ObjectSubtype> {

    @Override
    public ObjectSubtype toDTO(ObjectSubtype subtype) {
      return subtype;
    }

    @Override
    public ObjectSubtype toEntity(ObjectSubtype subtype) {
      return subtype;
    }

    @Override
    public Consumer<ObjectSubtype> entityApplyMethod(ObjectStoreMetadata entityRef) {
      return entityRef::setAcSubtype;
    }

    @Override
    public Supplier<ObjectSubtype> entitySupplyMethod(ObjectStoreMetadata entityRef) {
      return entityRef::getAcSubtype;
    }

    @Override
    public Consumer<ObjectSubtype> dtoApplyMethod(ObjectStoreMetadataDto dtoRef) {
      return dtoRef::applyObjectSubtype;
    }

    @Override
    public Supplier<ObjectSubtype> dtoSupplyMethod(ObjectStoreMetadataDto dtoRef) {
      return dtoRef::supplyObjectSubtype;
    }
  }

}
