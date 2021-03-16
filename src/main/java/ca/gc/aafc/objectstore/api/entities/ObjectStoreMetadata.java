package ca.gc.aafc.objectstore.api.entities;

import ca.gc.aafc.dina.entity.DinaEntity;
import ca.gc.aafc.dina.entity.SoftDeletable;
import com.vladmihalcea.hibernate.type.array.StringArrayType;
import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.NaturalIdCache;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The Class ObjectStoreMetadata.
 */
@Entity
@Table(name = "metadata")
@TypeDef(name = "pgsql_enum", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "string-array", typeClass = StringArrayType.class)
@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
@SuperBuilder
@AllArgsConstructor
@RequiredArgsConstructor
@NaturalIdCache
public class ObjectStoreMetadata extends BaseObject implements SoftDeletable, DinaEntity {

  private Integer id;

  private String dcFormat;
  private String acCaption;

  private OffsetDateTime acDigitizationDate;
  private OffsetDateTime xmpMetadataDate;

  private String xmpRightsWebStatement;
  private String dcRights;
  private String xmpRightsOwner;
  private String xmpRightsUsageTerms;

  private String originalFilename;

  private String[] acTags;

  private OffsetDateTime deletedDate;

  @Builder.Default
  private List<MetadataManagedAttribute> managedAttribute = new ArrayList<>();

  private UUID acMetadataCreator;
  private UUID dcCreator;

  private ObjectStoreMetadata acDerivedFrom;

  @Builder.Default
  private List<ObjectStoreMetadata> derivatives = new ArrayList<>();

  private Boolean publiclyReleasable;
  private String notPubliclyReleasableReason;

  private ObjectSubtype acSubType;

  /**
   * Read-only field to get the ac_sub_type_id to allow filtering by null values.
   */
  private Integer acSubTypeId;

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

  @Column(name = "dc_format")
  @Size(max = 150)
  public String getDcFormat() {
    return dcFormat;
  }

  public void setDcFormat(String dcFormat) {
    this.dcFormat = dcFormat;
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

  @OneToMany(mappedBy = "objectStoreMetadata", fetch = FetchType.LAZY)
  public List<MetadataManagedAttribute> getManagedAttribute() {
    return managedAttribute;
  }

  public void setManagedAttribute(List<MetadataManagedAttribute> managedAttribute) {
    this.managedAttribute = managedAttribute;
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

  @Override
  public OffsetDateTime getDeletedDate() {
    return deletedDate;
  }

  @Override
  public void setDeletedDate(OffsetDateTime deletedDate) {
    this.deletedDate = deletedDate;
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ac_derived_from_id", referencedColumnName = "id")
  public ObjectStoreMetadata getAcDerivedFrom() {
    return acDerivedFrom;
  }

  /**
   * Sets the acDerived from value. Note to establish and remove a bi directional relationship, the {@link
   * ObjectStoreMetadata#addDerivative} and {@link ObjectStoreMetadata#removeDerivative} methods should be
   * called from the parent.
   *
   * @param acDerivedFrom - parent to set
   */
  public void setAcDerivedFrom(ObjectStoreMetadata acDerivedFrom) {
    this.acDerivedFrom = acDerivedFrom;
  }

  @OneToMany(mappedBy = "acDerivedFrom", cascade = CascadeType.PERSIST)
  public List<ObjectStoreMetadata> getDerivatives() {
    return derivatives;
  }

  public void setDerivatives(List<ObjectStoreMetadata> derivatives) {
    this.derivatives = derivatives;
  }

  /**
   * Adds the given derivative to the list of derivatives. This method should be used to establish Bi
   * directional JPA relations ships.
   *
   * @param derivative - derivative to add
   */
  public void addDerivative(ObjectStoreMetadata derivative) {
    derivatives.add(derivative);
    derivative.setAcDerivedFrom(this);
  }

  /**
   * Adds the given derivative to the list of derivatives. This method should be used to remove Bi directional
   * JPA relations ships.
   *
   * @param derivative - derivative to remove
   */
  public void removeDerivative(ObjectStoreMetadata derivative) {
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
  public ObjectSubtype getAcSubType() {
    return acSubType;
  }

  public void setAcSubType(ObjectSubtype acSubType) {
    this.acSubType = acSubType;
  }

  /**
   * Read-only field to get the ac_derived_from_id to allow filtering by null values.
   */
  @Column(name = "ac_sub_type_id", updatable = false, insertable = false)
  public Integer getAcSubTypeId() {
    return acSubTypeId;
  }

  public void setAcSubTypeId(Integer acSubTypeId) {
    this.acSubTypeId = acSubTypeId;
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

}
