package ca.gc.aafc.objectstore.api.entities;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import ca.gc.aafc.dina.i18n.MultilingualTitle;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.hibernate.annotations.Type;

import ca.gc.aafc.dina.i18n.MultilingualDescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Entity(name = "managed_attribute")
@AllArgsConstructor
@Builder
@RequiredArgsConstructor
@NaturalIdCache
public class ObjectStoreManagedAttribute implements ca.gc.aafc.dina.entity.ManagedAttribute {

  private Integer id;
  private UUID uuid;
  private VocabularyElementType vocabularyElementType;
  private String[] acceptedValues;
  private OffsetDateTime createdOn;
  private String createdBy;
  private MultilingualDescription multilingualDescription;
  
  @Column(updatable = false)
  private String name;

  @NotBlank
  @Size(max = 50)
  @Column(updatable = false)
  @Getter
  @Setter
  private String key;

  @NotNull
  @Type(JsonType.class)
  @Column(name = "multilingual_description", columnDefinition = "jsonb")
  public MultilingualDescription getMultilingualDescription() {
    return multilingualDescription;
  }

  public void setMultilingualDescription(MultilingualDescription multilingualDescription) {
    this.multilingualDescription = multilingualDescription;
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
  @NotNull
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
  @Type(PostgreSQLEnumType.class)
  @Enumerated(EnumType.STRING)
  @Column(name = "type")
  public VocabularyElementType getVocabularyElementType() {
    return vocabularyElementType;
  }

  public void setVocabularyElementType(VocabularyElementType type) {
    this.vocabularyElementType = type;
  }

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

  // not implemented for now
  @Transient
  @Override
  public String getTerm() {
    return null;
  }

  // not implemented for now
  @Transient
  @Override
  public String getUnit() {
    return null;
  }

  // not implemented for now
  @Transient
  @Override
  public MultilingualTitle getMultilingualTitle() {
    return null;
  }
}
