package ca.gc.aafc.objectstore.api.entities;

import ca.gc.aafc.dina.entity.DinaEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "object_upload")
@Builder(toBuilder = true)
@AllArgsConstructor
@RequiredArgsConstructor
@NaturalIdCache
public class ObjectUpload implements DinaEntity {

  private Integer id;
  private UUID fileIdentifier;
  private DcType dcType;
  private String createdBy;
  private OffsetDateTime createdOn;
  private String originalFilename;
  private String sha1Hex;
  private String receivedMediaType;
  private String detectedMediaType;
  private String detectedFileExtension;
  private String evaluatedMediaType;
  private String evaluatedFileExtension;
  private long sizeInBytes;
  private String bucket;
  private Map<String, String> exif;
  private String dateTimeDigitized;
  private boolean isDerivative;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Override
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  @Override
  @Transient
  public UUID getUuid() {
    return fileIdentifier;
  }

  @NaturalId
  @NotNull
  @Column(name = "file_identifier", unique = true)
  public UUID getFileIdentifier() {
    return fileIdentifier;
  }

  public void setFileIdentifier(UUID fileIdentifier) {
    this.fileIdentifier = fileIdentifier;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  @NotBlank
  @Column(name = "created_by", updatable = false)
  public String getCreatedBy() {
    return this.createdBy;
  }

  public void setCreatedOn(OffsetDateTime createdOn) {
    this.createdOn = createdOn;
  }

  @Column(name = "created_on", insertable = false, updatable = false)
  @Generated(value = GenerationTime.INSERT)
  public OffsetDateTime getCreatedOn() {
    return this.createdOn;
  }

  @NotNull
  @Column(name = "original_filename")
  @Size(max = 250)
  public String getOriginalFilename() {
    return originalFilename;
  }

  public void setOriginalFilename(String originalFilename) {
    this.originalFilename = originalFilename;
  }

  @NotNull
  @Column(name = "sha1_hex")
  @Size(max = 128)
  public String getSha1Hex() {
    return sha1Hex;
  }

  public void setSha1Hex(String sha1Hex) {
    this.sha1Hex = sha1Hex;
  }

  @Column(name = "received_media_type")
  @Size(max = 150)
  public String getReceivedMediaType() {
    return receivedMediaType;
  }

  public void setReceivedMediaType(String receivedMediaType) {
    this.receivedMediaType = receivedMediaType;
  }

  @Column(name = "detected_media_type")
  @Size(max = 150)
  public String getDetectedMediaType() {
    return detectedMediaType;
  }

  public void setDetectedMediaType(String detectedMediaType) {
    this.detectedMediaType = detectedMediaType;
  }

  @Column(name = "detected_file_extension")
  @Size(max = 10)
  public String getDetectedFileExtension() {
    return detectedFileExtension;
  }

  public void setDetectedFileExtension(String detectedFileExtension) {
    this.detectedFileExtension = detectedFileExtension;
  }

  @Column(name = "evaluated_media_type")
  @Size(max = 150)
  public String getEvaluatedMediaType() {
    return evaluatedMediaType;
  }

  public void setEvaluatedMediaType(String evaluatedMediaType) {
    this.evaluatedMediaType = evaluatedMediaType;
  }

  @Column(name = "evaluated_file_extension")
  @Size(max = 10)
  public String getEvaluatedFileExtension() {
    return evaluatedFileExtension;
  }

  public void setEvaluatedFileExtension(String evaluatedFileExtension) {
    this.evaluatedFileExtension = evaluatedFileExtension;
  }

  @Column(name = "size_in_bytes")
  public long getSizeInBytes() {
    return sizeInBytes;
  }

  public void setSizeInBytes(long sizeInBytes) {
    this.sizeInBytes = sizeInBytes;
  }

  @NotNull
  @Size(max = 50)
  public String getBucket() {
    return bucket;
  }

  public void setBucket(String bucket) {
    this.bucket = bucket;
  }

  @Size(max = 50)
  public String getDateTimeDigitized() {
    return dateTimeDigitized;
  }

  public void setDateTimeDigitized(String dateTimeDigitized) {
    this.dateTimeDigitized = dateTimeDigitized;
  }

  @Type(type = "jsonb")
  @Column(name = "exif", columnDefinition = "jsonb")
  public Map<String, String> getExif() {
    return exif;
  }

  public void setExif(Map<String, String> exif) {
    this.exif = exif;
  }

  @Type(type = "pgsql_enum")
  @Enumerated(EnumType.STRING)
  @NotNull
  @Column(name = "dc_type")
  public DcType getDcType() {
    return dcType;
  }

  public void setDcType(DcType dcType) {
    this.dcType = dcType;
  }

  @NotNull
  @Column(name = "is_derivative")
  public boolean getIsDerivative() {
    return isDerivative;
  }

  public void setIsDerivative(boolean derivative) {
    isDerivative = derivative;
  }

  @Transient
  public String getCompleteFileName() {
    return this.getFileIdentifier() + this.getEvaluatedFileExtension();
  }
}
