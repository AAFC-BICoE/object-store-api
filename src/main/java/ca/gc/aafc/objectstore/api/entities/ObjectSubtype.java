package ca.gc.aafc.objectstore.api.entities;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import ca.gc.aafc.dina.entity.DinaEntity;

import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.hibernate.type.SqlTypes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "object_subtype")
@Builder(toBuilder = true)
@AllArgsConstructor
@RequiredArgsConstructor
@NaturalIdCache
public class ObjectSubtype implements DinaEntity {

  private Integer id;
  private DcType dcType;
  private String acSubtype;
  private UUID uuid;
  private String createdBy;
  private OffsetDateTime createdOn;

  @NaturalId
  @NotNull
  @Column(name = "uuid", unique = true)
  public UUID getUuid() {
    return uuid;
  }

  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }

  @Column(name = "ac_subtype")
  @Size(max = 50)
  @ColumnTransformer(write = "UPPER(TRIM(?))")
  @NotBlank
  public String getAcSubtype() {
    return acSubtype;
  }

  public void setAcSubtype(String acSubtype) {
    this.acSubtype = acSubtype;
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  @NotNull
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Enumerated(EnumType.STRING)
  @Column(name = "dc_type")
  public DcType getDcType() {
    return dcType;
  }

  public void setDcType(DcType dcType) {
    this.dcType = dcType;
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

}
