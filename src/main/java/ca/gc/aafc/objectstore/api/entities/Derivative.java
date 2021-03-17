package ca.gc.aafc.objectstore.api.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "derivative")
@NoArgsConstructor
@AllArgsConstructor
public class Derivative extends AbstractObjectStoreMetadata {

  private Integer id;
  private ObjectSubtype objectSubtype;
  private ObjectStoreMetadata acDerivedFrom;

  @Builder
  public Derivative(
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
    ObjectSubtype objectSubtype,
    ObjectStoreMetadata acDerivedFrom
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
      createdOn);
    this.id = id;
    this.objectSubtype = objectSubtype;
    this.acDerivedFrom = acDerivedFrom;
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

  @ManyToOne
  @JoinColumn(name = "object_subtype", referencedColumnName = "id")
  public ObjectSubtype getObjectSubtype() {
    return objectSubtype;
  }

  public void setObjectSubtype(ObjectSubtype objectSubtype) {
    this.objectSubtype = objectSubtype;
  }

  @ManyToOne
  @JoinColumn(name = "ac_derived_from", referencedColumnName = "id")
  public ObjectStoreMetadata getAcDerivedFrom() {
    return acDerivedFrom;
  }

  public void setAcDerivedFrom(ObjectStoreMetadata acDerivedFrom) {
    this.acDerivedFrom = acDerivedFrom;
  }

}
