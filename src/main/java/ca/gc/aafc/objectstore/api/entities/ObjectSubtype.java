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
import ca.gc.aafc.dina.i18n.MultilingualDescription;
import ca.gc.aafc.dina.i18n.MultilingualTitle;
import io.hypersistence.utils.hibernate.type.json.JsonType;

import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.hibernate.annotations.Type;
import org.hibernate.generator.EventType;
import org.hibernate.type.SqlTypes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "object_subtype")
@Builder(toBuilder = true)
@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
@NaturalIdCache
public class ObjectSubtype implements DinaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @NotNull
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Enumerated(EnumType.STRING)
  @Column(name = "dc_type")
  private DcType dcType;

  @Column(name = "ac_subtype")
  @Size(max = 50)
  @ColumnTransformer(write = "UPPER(TRIM(?))")
  @NotBlank
  private String acSubtype;

  @NaturalId
  @NotNull
  @Column(name = "uuid", unique = true)
  private UUID uuid;

  @Column(name = "system_defined", nullable = false, insertable = false, updatable = false)
  @Generated(event = EventType.INSERT)
  private Boolean systemDefined;

  @Column(name = "multilingual_title", columnDefinition = "jsonb")
  @Type(JsonType.class)
  private MultilingualTitle multilingualTitle;

  @Column(name = "multilingual_description", columnDefinition = "jsonb")
  @Type(JsonType.class)
  private MultilingualDescription multilingualDescription;

  @NotBlank
  @Column(name = "created_by", updatable = false)
  private String createdBy;

  @Column(name = "created_on", insertable = false, updatable = false)
  @Generated(value = GenerationTime.INSERT)
  private OffsetDateTime createdOn;

}
