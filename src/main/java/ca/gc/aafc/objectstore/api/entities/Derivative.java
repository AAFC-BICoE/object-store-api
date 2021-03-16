package ca.gc.aafc.objectstore.api.entities;

import ca.gc.aafc.dina.entity.DinaEntity;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "derivative")
@SuperBuilder
@AllArgsConstructor
@RequiredArgsConstructor
public class Derivative extends BaseObject implements DinaEntity {

  private Integer id;
  private ObjectSubtype objectSubtype;

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
}
