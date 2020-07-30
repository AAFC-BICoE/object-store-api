package ca.gc.aafc.objectstore.api.entities;

import java.time.OffsetDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.NaturalId;

import ca.gc.aafc.dina.entity.DinaEntity;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Entity
@Builder(toBuilder = true)
@AllArgsConstructor
@RequiredArgsConstructor
@SuppressFBWarnings(justification = "ok for Hibernate Entity", value = { "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
public class MetadataManagedAttribute implements DinaEntity {

  private Integer id;
  private UUID uuid;

  private ObjectStoreMetadata objectStoreMetadata;
  private ManagedAttribute managedAttribute;
  private String assignedValue;

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
  @ManyToOne(cascade = {})
  @JoinColumn(name = "metadata_id", referencedColumnName = "id")
  public ObjectStoreMetadata getObjectStoreMetadata() {
    return objectStoreMetadata;
  }

  public void setObjectStoreMetadata(ObjectStoreMetadata objectStoreMetadata) {
    this.objectStoreMetadata = objectStoreMetadata;
  }

  @NotNull
  @ManyToOne(cascade = {})
  @JoinColumn(name = "managed_attribute_id", referencedColumnName = "id")
  public ManagedAttribute getManagedAttribute() {
    return managedAttribute;
  }

  public void setManagedAttribute(ManagedAttribute managedAttribute) {
    this.managedAttribute = managedAttribute;
  }

  public String getAssignedValue() {
    return assignedValue;
  }

  public void setAssignedValue(String assignedValue) {
    this.assignedValue = assignedValue;
  }

  public String toString() {
    return new ToStringBuilder(this).append("id", id).append("uuid", uuid).append("assignedValue", assignedValue)
        .append("managedAttribute", managedAttribute).append("objectStoreMetadata", objectStoreMetadata).toString();
  }

  @PrePersist
  public void init() {
    this.uuid = UUID.randomUUID();
    this.updateParentMetadata();
  }
  
  /**
   * MetadataManagedAttribute is considered a child value of ObjectStoreMetadata,
   * so update the parent whenever this is modified.
   * 
   * This helps for auditing.
   */
  @PreUpdate
  @PreRemove
  public void updateParentMetadata() {
    this.objectStoreMetadata.setXmpMetadataDate(OffsetDateTime.now());
  }

}
