package ca.gc.aafc.objectstore.api.entities;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.vladmihalcea.hibernate.type.array.StringArrayType;
import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.UpdateTimestamp;

import ca.gc.aafc.dina.entity.SoftDeletable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

/**
 * The Class ObjectStoreMetadata.
 */
@Entity
@Table(name = "metadata")
@TypeDef(name = "pgsql_enum", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "string-array", typeClass = StringArrayType.class)
@SuppressFBWarnings({ "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
@Builder(toBuilder = true)
@AllArgsConstructor
@RequiredArgsConstructor
@NaturalIdCache
public class ObjectStoreMetadata implements SoftDeletable {

  private Integer id;

  private UUID uuid;
  private String bucket;
  private UUID fileIdentifier;
  private String fileExtension;

  private String dcFormat;
  private DcType dcType;
  private String acCaption;

  private OffsetDateTime acDigitizationDate;
  private OffsetDateTime xmpMetadataDate;

  private String xmpRightsWebStatement;
  private String dcRights;
  private String xmpRightsOwner;

  private String originalFilename;

  private String acHashFunction;
  private String acHashValue;
  private String[] acTags;

  private OffsetDateTime createdDate;
  private OffsetDateTime deletedDate;

  @Builder.Default
  private List<MetadataManagedAttribute> managedAttribute = new ArrayList<>();
  private Agent acMetadataCreator;
  private Agent dcCreator;

  private ObjectStoreMetadata acDerivedFrom;

  private boolean publiclyReleasable;
  private String notPubliclyReleasableReason;

  private ObjectSubtype acSubType;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  @NaturalId
  @NotNull
  @Column(name = "uuid", unique = true)
  public UUID getUuid() {
    return uuid;
  }

  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }

  @NotNull
  @Column(name = "file_identifier", unique = true)
  public UUID getFileIdentifier() {
    return fileIdentifier;
  }

  public void setFileIdentifier(UUID fileIdentifier) {
    this.fileIdentifier = fileIdentifier;
  }

  @NotNull
  @Column(name = "file_extension")
  @Size(max = 10)
  public String getFileExtension() {
    return fileExtension;
  }

  public void setFileExtension(String fileExtension) {
    this.fileExtension = fileExtension;
  }

  /**
   * Returns ileIdentifier + fileExtension
   * 
   * @return
   */
  @Transient
  public String getFilename() {
    return fileIdentifier + fileExtension;
  }

  @NotNull
  @Size(max = 50)
  public String getBucket() {
    return bucket;
  }

  public void setBucket(String bucket) {
    this.bucket = bucket;
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

  @NotNull
  @Type(type = "pgsql_enum")
  @Enumerated(EnumType.STRING)
  @Column(name = "dc_type")
  public DcType getDcType() {
    return dcType;
  }

  public void setDcType(DcType dcType) {
    this.dcType = dcType;
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

  @Column(name = "ac_hash_function")
  public String getAcHashFunction() {
    return acHashFunction;
  }

  public void setAcHashFunction(String acHashFunction) {
    this.acHashFunction = acHashFunction;
  }

  @Column(name = "ac_hash_value")
  public String getAcHashValue() {
    return acHashValue;
  }

  public void setAcHashValue(String acHashValue) {
    this.acHashValue = acHashValue;
  }

  @Type(type = "string-array")
  @Column(name = "ac_tags", columnDefinition = "text[]")
  public String[] getAcTags() {
    return acTags;
  }

  public void setAcTags(String[] acTags) {
    this.acTags = acTags;
  }

  @Column(name = "created_date", insertable = false, updatable = false)
  public OffsetDateTime getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(OffsetDateTime createdDate) {
    this.createdDate = createdDate;
  }

  @OneToMany
  @JoinColumn(name = "metadata_id")
  public List<MetadataManagedAttribute> getManagedAttribute() {
    return managedAttribute;
  }

  public void setManagedAttribute(List<MetadataManagedAttribute> managedAttribute) {
    this.managedAttribute = managedAttribute;
  }

  @OneToOne
  @JoinColumn(name = "ac_metadata_creator_id", referencedColumnName = "id")
  public Agent getAcMetadataCreator() {
    return acMetadataCreator;
  }

  public void setAcMetadataCreator(Agent acMetadataCreator) {
    this.acMetadataCreator = acMetadataCreator;
  }

  @OneToOne
  @JoinColumn(name = "dc_creator_id", referencedColumnName = "id")
  public Agent getDcCreator() {
    return dcCreator;
  }

  public void setDcCreator(Agent dcCreator) {
    this.dcCreator = dcCreator;
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

  @OneToOne
  @JoinColumn(name = "ac_derived_from_id", referencedColumnName = "id")
  public ObjectStoreMetadata getAcDerivedFrom() {
    return acDerivedFrom;
  }

  public void setAcDerivedFrom(ObjectStoreMetadata acDerivedFrom) {
    this.acDerivedFrom = acDerivedFrom;
  }

  @Column(name = "publicly_releasable")
  public boolean getPubliclyReleasable() {
    return publiclyReleasable;
  }

  public void setPubliclyReleasable(boolean publiclyReleasable) {
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

  // TODO: Fix dina-base-api modifyRelation method so it doesn't fail when the
  // DTO has a relation that the entity doesn't.
  @Deprecated
  @Transient
  public Object getManagedAttributeMap() {
    return null;
  }
  
  // TODO: Fix dina-base-api modifyRelation method so it doesn't fail when the
  // DTO has a relation that the entity doesn't.
  @Deprecated
  @Transient
  public void setManagedAttributeMap(Object object) {
  }

  @PrePersist
  public void initUuid() {
    this.uuid = UUID.randomUUID();
  }

}
