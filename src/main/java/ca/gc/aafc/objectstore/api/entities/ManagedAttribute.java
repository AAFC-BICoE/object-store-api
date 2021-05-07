package ca.gc.aafc.objectstore.api.entities;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.vladmihalcea.hibernate.type.array.StringArrayType;
import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import ca.gc.aafc.dina.entity.DinaEntity;
import ca.gc.aafc.dina.service.OnUpdate;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Entity
@TypeDefs({@TypeDef(name = "pgsql_enum", typeClass = PostgreSQLEnumType.class),
  @TypeDef(name = "string-array", typeClass = StringArrayType.class),
  @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)})
@AllArgsConstructor
@Builder
@RequiredArgsConstructor
@SuppressFBWarnings(justification = "ok for Hibernate Entity", value = { "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
@NaturalIdCache
public class ManagedAttribute implements DinaEntity {

  public enum ManagedAttributeType {
    INTEGER, STRING
  }

  private Integer id;
  private UUID uuid;
  private String name;
  private ManagedAttributeType managedAttributeType;
  private String[] acceptedValues;
  private OffsetDateTime createdOn;
  private String createdBy;
  private Map<String, String> description;

  @Type(type = "jsonb")
  @Column(name = "description", columnDefinition = "jsonb")
  @NotEmpty
  public Map<String, String> getDescription() {
    return description;
  }

  public void setDescription(Map<String, String> description) {
    this.description = description;
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  @NaturalId
  @NotNull(groups = OnUpdate.class)
  @Column(name = "uuid", unique = true)
  public UUID getUuid() {
    return uuid;
  }

  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }

  @NotNull
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @NotNull
  @Type(type = "pgsql_enum")
  @Enumerated(EnumType.STRING)
  @Column(name = "type")
  public ManagedAttributeType getManagedAttributeType() {
    return managedAttributeType;
  }

  public void setManagedAttributeType(ManagedAttributeType type) {
    this.managedAttributeType = type;
  }

  @Type(type = "string-array")
  @Column(columnDefinition = "text[]")
  public String[] getAcceptedValues() {
    return acceptedValues;
  }

  public void setAcceptedValues(String[] acceptedValues) {
    this.acceptedValues = acceptedValues;
  }

  @Column(name = "created_on", insertable = false, updatable = false)
  @Generated(value = GenerationTime.INSERT)
  public OffsetDateTime getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(OffsetDateTime createdOn) {
    this.createdOn = createdOn;
  }

  @NotBlank
  @Column(name = "created_by", updatable = false)
  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

}
