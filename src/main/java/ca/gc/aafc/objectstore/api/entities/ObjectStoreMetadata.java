package ca.gc.aafc.objectstore.api.entities;

import com.vladmihalcea.hibernate.type.array.StringArrayType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The Class ObjectStoreMetadata.
 */
@Entity
@Table(name = "metadata")
@TypeDef(name = "string-array", typeClass = StringArrayType.class)
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
@RequiredArgsConstructor
public class ObjectStoreMetadata extends AbstractObjectStoreMetadata {

  private Integer id;

  private String acCaption;

  private OffsetDateTime acDigitizationDate;
  private OffsetDateTime xmpMetadataDate;

  private String xmpRightsWebStatement;
  private String dcRights;
  private String xmpRightsOwner;
  private String xmpRightsUsageTerms;

  private String originalFilename;

  private String[] acTags;

  private Map<String, String> managedAttributeValues;

  private UUID acMetadataCreator;
  private UUID dcCreator;

  private List<Derivative> derivatives;

  private Boolean publiclyReleasable;
  private String notPubliclyReleasableReason;

  private ObjectSubtype acSubtype;

  private Integer orientation;

  /**
   * Read-only field to get the ac_sub_type_id to allow filtering by null values.
   */
  private Integer acSubtypeId;

  @Builder
  public ObjectStoreMetadata(
    UUID uuid,
    String bucket,
    UUID fileIdentifier,
    String fileExtension,
    DcType dcType,
    String acHashFunction,
    String acHashValue,
    String createdBy,
    OffsetDateTime createdOn,
    Integer id,
    String dcFormat,
    String acCaption,
    OffsetDateTime acDigitizationDate,
    OffsetDateTime xmpMetadataDate,
    String xmpRightsWebStatement,
    String dcRights,
    String xmpRightsOwner,
    String xmpRightsUsageTerms,
    String originalFilename,
    String[] acTags,
    Map<String, String> managedAttributeValues,
    UUID acMetadataCreator,
    UUID dcCreator,
    List<Derivative> derivatives,
    Boolean publiclyReleasable,
    String notPubliclyReleasableReason,
    ObjectSubtype acSubtype,
    Integer acSubtypeId,
    Integer orientation
  ) {
    super(
      uuid,
      bucket,
      fileIdentifier,
      fileExtension,
      dcType,
      acHashFunction,
      acHashValue,
      createdBy,
      createdOn,
      dcFormat);
    this.id = id;
    this.acCaption = acCaption;
    this.acDigitizationDate = acDigitizationDate;
    this.xmpMetadataDate = xmpMetadataDate;
    this.xmpRightsWebStatement = xmpRightsWebStatement;
    this.dcRights = dcRights;
    this.xmpRightsOwner = xmpRightsOwner;
    this.xmpRightsUsageTerms = xmpRightsUsageTerms;
    this.originalFilename = originalFilename;
    this.acTags = acTags;
    this.managedAttributeValues = MapUtils.isNotEmpty(managedAttributeValues) ? managedAttributeValues : new HashMap<>();
    this.acMetadataCreator = acMetadataCreator;
    this.dcCreator = dcCreator;
    this.derivatives = CollectionUtils.isNotEmpty(derivatives) ? derivatives : new ArrayList<>();
    this.publiclyReleasable = publiclyReleasable;
    this.notPubliclyReleasableReason = notPubliclyReleasableReason;
    this.acSubtype = acSubtype;
    this.acSubtypeId = acSubtypeId;
    this.orientation = orientation;
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  /**
   * Returns fileIdentifier + fileExtension
   *
   * @return fileIdentifier + fileExtension
   */
  @Transient
  public String getFilename() {
    return fileIdentifier + fileExtension;
  }

  @Column(name = "ac_caption")
  @Size(max = 250)
  public String getAcCaption() {
    return acCaption;
  }

  public void setAcCaption(String acCaption) {
    this.acCaption = acCaption;
  }

  @Column(name = "ac_digitization_date")
  public OffsetDateTime getAcDigitizationDate() {
    return acDigitizationDate;
  }

  public void setAcDigitizationDate(OffsetDateTime acDigitizationDate) {
    this.acDigitizationDate = acDigitizationDate;
  }

  @UpdateTimestamp
  @Column(name = "xmp_metadata_date")
  public OffsetDateTime getXmpMetadataDate() {
    return xmpMetadataDate;
  }

  public void setXmpMetadataDate(OffsetDateTime xmpMetadataDate) {
    this.xmpMetadataDate = xmpMetadataDate;
  }

  @Column(name = "original_filename")
  public String getOriginalFilename() {
    return originalFilename;
  }

  public void setOriginalFilename(String originalFilename) {
    this.originalFilename = originalFilename;
  }

  @Type(type = "string-array")
  @Column(name = "ac_tags", columnDefinition = "text[]")
  public String[] getAcTags() {
    return acTags;
  }

  public void setAcTags(String[] acTags) {
    this.acTags = acTags;
  }

  @Transient
  @Deprecated
  public OffsetDateTime getCreatedDate() {
    return createdOn;
  }

  @Deprecated
  public void setCreatedDate(OffsetDateTime createdDate) {
    this.createdOn = createdDate;
  }

  @NotNull
  @Column(name = "xmp_rights_web_statement")
  @Size(max = 250)
  public String getXmpRightsWebStatement() {
    return xmpRightsWebStatement;
  }

  public void setXmpRightsWebStatement(String xmpRightsWebStatement) {
    this.xmpRightsWebStatement = xmpRightsWebStatement;
  }

  @NotNull
  @Column(name = "ac_rights")
  @Size(max = 250)
  public String getDcRights() {
    return dcRights;
  }

  public void setDcRights(String dcRights) {
    this.dcRights = dcRights;
  }

  @NotNull
  @Column(name = "xmp_rights_owner")
  @Size(max = 250)
  public String getXmpRightsOwner() {
    return xmpRightsOwner;
  }

  public void setXmpRightsOwner(String xmpRightsOwner) {
    this.xmpRightsOwner = xmpRightsOwner;
  }

  @OneToMany(mappedBy = "acDerivedFrom", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
  public List<Derivative> getDerivatives() {
    return derivatives;
  }

  public void setDerivatives(List<Derivative> derivatives) {
    this.derivatives = derivatives;
  }

  /**
   * Adds the given derivative to the list of derivatives. This method should be used to establish Bi
   * directional JPA relations ships.
   *
   * @param derivative - derivative to add
   */
  public void addDerivative(Derivative derivative) {
    derivatives.add(derivative);
    derivative.setAcDerivedFrom(this);
  }

  /**
   * Adds the given derivative to the list of derivatives. This method should be used to remove Bi directional
   * JPA relations ships.
   *
   * @param derivative - derivative to remove
   */
  public void removeDerivative(Derivative derivative) {
    derivatives.remove(derivative);
    derivative.setAcDerivedFrom(null);
  }

  @Column(name = "publicly_releasable")
  public Boolean getPubliclyReleasable() {
    return publiclyReleasable;
  }

  public void setPubliclyReleasable(Boolean publiclyReleasable) {
    this.publiclyReleasable = publiclyReleasable;
  }

  @Column(name = "not_publicly_releasable_reason")
  public String getNotPubliclyReleasableReason() {
    return notPubliclyReleasableReason;
  }

  public void setNotPubliclyReleasableReason(String notPubliclyReleasableReason) {
    this.notPubliclyReleasableReason = notPubliclyReleasableReason;
  }

  @ManyToOne
  @JoinColumn(name = "ac_sub_type_id", referencedColumnName = "id")
  public ObjectSubtype getAcSubtype() {
    return acSubtype;
  }

  public void setAcSubtype(ObjectSubtype acSubtype) {
    this.acSubtype = acSubtype;
  }

  /**
   * Read-only field to get the ac_derived_from_id to allow filtering by null values.
   */
  @Column(name = "ac_sub_type_id", updatable = false, insertable = false)
  public Integer getAcSubtypeId() {
    return acSubtypeId;
  }

  public void setAcSubtypeId(Integer acSubtypeId) {
    this.acSubtypeId = acSubtypeId;
  }

  @Column(name = "ac_metadata_creator_id")
  public UUID getAcMetadataCreator() {
    return acMetadataCreator;
  }

  public void setAcMetadataCreator(UUID acMetadataCreator) {
    this.acMetadataCreator = acMetadataCreator;
  }

  @Column(name = "dc_creator_id")
  public UUID getDcCreator() {
    return dcCreator;
  }

  public void setDcCreator(UUID dcCreator) {
    this.dcCreator = dcCreator;
  }

  @NotNull
  @Column(name = "xmp_rights_usage_terms")
  @Size(max = 500)
  public String getXmpRightsUsageTerms() {
    return xmpRightsUsageTerms;
  }

  public void setXmpRightsUsageTerms(String xmpRightsUsageTerms) {
    this.xmpRightsUsageTerms = xmpRightsUsageTerms;
  }

  @Min(value = 1)
  @Max(value = 8)
  public Integer getOrientation() {
    return orientation;
  }

  public void setOrientation(Integer orientation) {
    this.orientation = orientation;
  }

  /**
   * Transient field until base implementation is ready
   **/
  @Transient
  public String getGroup() {
    return bucket;
  }

  /**
   * Empty setter method to avoid resource method error: missing accessor method until multiple custom field
   * resolvers can be associated with single resource
   */
  public void setGroup(String group) {
  }

  @Type(type = "jsonb")
  @NotNull
  public Map<String, String> getManagedAttributeValues() {
    return managedAttributeValues;
  }

  public void setManagedAttributeValues(Map<String, String> managedAttributeValues) {
    this.managedAttributeValues = managedAttributeValues;
  }
}
