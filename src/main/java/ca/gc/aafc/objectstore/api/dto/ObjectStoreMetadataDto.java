package ca.gc.aafc.objectstore.api.dto;

import ca.gc.aafc.dina.dto.ExternalRelationDto;
import ca.gc.aafc.dina.dto.RelatedEntity;
import ca.gc.aafc.dina.mapper.CustomFieldAdapter;
import ca.gc.aafc.dina.mapper.DinaFieldAdapter;
import ca.gc.aafc.dina.mapper.IgnoreDinaMapping;
import ca.gc.aafc.dina.repository.meta.JsonApiExternalRelation;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.javers.core.metamodel.annotation.DiffIgnore;
import org.javers.core.metamodel.annotation.Id;
import org.javers.core.metamodel.annotation.PropertyName;
import org.javers.core.metamodel.annotation.ShallowReference;
import org.javers.core.metamodel.annotation.TypeName;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
@RelatedEntity(ObjectStoreMetadata.class)
@Data
@JsonApiResource(type = ObjectStoreMetadataDto.TYPENAME)
@TypeName(ObjectStoreMetadataDto.TYPENAME)
@CustomFieldAdapter(adapters = ObjectStoreMetadataDto.AcSubTypeAdapter.class)
public class ObjectStoreMetadataDto {

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

  @JsonInclude(Include.NON_EMPTY)
  private String originalFilename;

  private String acHashFunction;
  private String acHashValue;

  @DiffIgnore
  private OffsetDateTime createdDate;
  @JsonInclude(Include.NON_EMPTY)
  private OffsetDateTime deletedDate;

  @JsonInclude(Include.NON_EMPTY)
  private String[] acTags;

  @JsonApiRelation
  @DiffIgnore
  private List<MetadataManagedAttributeDto> managedAttribute;

  // AUTOMATICALLY_ALWAYS because it should be fetched using a call to
  // MetadataToManagedAttributeMapRepository.
  @JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS)
  private ManagedAttributeMapDto managedAttributeMap;

  @JsonApiExternalRelation(type = "person")
  @JsonApiRelation
  private ExternalRelationDto acMetadataCreator;

  @JsonApiRelation
  @ShallowReference
  private ObjectStoreMetadataDto acDerivedFrom;

  @JsonApiRelation
  @DiffIgnore
  private List<ObjectStoreMetadataDto> derivatives = new ArrayList<>();

  @JsonApiExternalRelation(type = "person")
  @JsonApiRelation
  private ExternalRelationDto dcCreator;

  private boolean publiclyReleasable;

  @JsonInclude(Include.NON_EMPTY)
  private String notPubliclyReleasableReason;

  @JsonInclude(Include.NON_EMPTY)
  @IgnoreDinaMapping(reason = "Custom Resolved field")
  private String acSubType;

  private String group;

  public static class AcSubTypeAdapter
    implements DinaFieldAdapter<ObjectStoreMetadataDto, ObjectStoreMetadata, String, ObjectSubtype> {

    private static final String SPLITTER = "/";

    @Override
    public String toDTO(ObjectSubtype objectSubtype) {
      return objectSubtype == null ? null : objectSubtype.getAcSubtype();
    }

    @Override
    public ObjectSubtype toEntity(String s) {
      if (StringUtils.isBlank(s)) {
        return null;
      }
      String[] parts = s.split(SPLITTER);
      return ObjectSubtype.builder()
        .dcType(DcType.fromValue(parts[0]).orElse(null))
        .acSubtype(parts[1])
        .build();
    }

    @Override
    public Consumer<ObjectSubtype> entityApplyMethod(ObjectStoreMetadata entityRef) {
      return entityRef::setAcSubType;
    }

    @Override
    public Consumer<String> dtoApplyMethod(ObjectStoreMetadataDto dtoRef) {
      return dtoRef::setAcSubType;
    }

    @Override
    public Supplier<ObjectSubtype> entitySupplyMethod(ObjectStoreMetadata entityRef) {
      return entityRef::getAcSubType;
    }

    @Override
    public Supplier<String> dtoSupplyMethod(ObjectStoreMetadataDto dtoRef) {
      if (dtoRef.getDcType() == null || StringUtils.isBlank(dtoRef.getAcSubType())) {
        return () -> null;
      }
      return () -> dtoRef.getDcType() + SPLITTER + dtoRef.getAcSubType();
    }
  }

}
