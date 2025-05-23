package ca.gc.aafc.objectstore.api.entities;

import ca.gc.aafc.dina.entity.DinaEntity;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@MappedSuperclass
@AllArgsConstructor
@RequiredArgsConstructor
@NaturalIdCache
public abstract class AbstractObjectStoreMetadata implements DinaEntity {
  protected UUID uuid;
  protected String bucket;
  protected UUID fileIdentifier;
  protected String fileExtension;
  protected DcType dcType;
  protected String acHashFunction;
  protected String acHashValue;
  protected String createdBy;
  protected OffsetDateTime createdOn;
  private String dcFormat;
  private Boolean publiclyReleasable;
  private String notPubliclyReleasableReason;

  private String[] acTags;

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
  @Size(max = 50)
  public String getBucket() {
    return bucket;
  }

  public void setBucket(String bucket) {
    this.bucket = bucket;
  }

  @Column(name = "file_identifier", unique = true)
  public UUID getFileIdentifier() {
    return fileIdentifier;
  }

  public void setFileIdentifier(UUID fileIdentifier) {
    this.fileIdentifier = fileIdentifier;
  }

  @Column(name = "file_extension")
  @Size(max = 10)
  public String getFileExtension() {
    return fileExtension;
  }

  public void setFileExtension(String fileExtension) {
    this.fileExtension = fileExtension;
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

  @Column(name = "dc_format")
  @NotNull
  @Size(max = 150)
  public String getDcFormat() {
    return dcFormat;
  }

  public void setDcFormat(String dcFormat) {
    this.dcFormat = dcFormat;
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

  @NotBlank
  @Column(name = "created_by", updatable = false)
  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  @Column(name = "created_on", insertable = false, updatable = false)
  @Generated(value = GenerationTime.INSERT)
  public OffsetDateTime getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(OffsetDateTime createdOn) {
    this.createdOn = createdOn;
  }

  @Column(name = "publicly_releasable")
  public Boolean getPubliclyReleasable() {
    return publiclyReleasable;
  }

  public void setPubliclyReleasable(Boolean publiclyReleasable) {
    this.publiclyReleasable = publiclyReleasable;
  }

    /**
   * Return publiclyReleasable as Optional as defined by
   * {@link ca.gc.aafc.dina.entity.DinaEntity}.
   * 
   * @return
   */
  @Override
  @Transient
  public Optional<Boolean> isPubliclyReleasable() {
    return Optional.ofNullable(publiclyReleasable);
  }

  @Column(name = "not_publicly_releasable_reason")
  public String getNotPubliclyReleasableReason() {
    return notPubliclyReleasableReason;
  }

  public void setNotPubliclyReleasableReason(String notPubliclyReleasableReason) {
    this.notPubliclyReleasableReason = notPubliclyReleasableReason;
  }

  @Type(type = "string-array")
  @Column(name = "ac_tags", columnDefinition = "text[]")
  public String[] getAcTags() {
    return acTags;
  }

  public void setAcTags(String[] acTags) {
    this.acTags = acTags;
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
}
