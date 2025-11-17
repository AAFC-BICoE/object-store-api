package ca.gc.aafc.objectstore.api.entities;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "derivative")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Derivative extends AbstractObjectStoreMetadata {

  public enum DerivativeType {
    THUMBNAIL_IMAGE("thumbnail"), LARGE_IMAGE("large"), CROPPED_IMAGE("cropped");

    private final String suffix;

    DerivativeType(String suffix) {
      this.suffix = suffix;
    }

    public String getSuffix() {
      return suffix;
    }
  }

  public static final String AC_DERIVED_FROM_PROP = "acDerivedFrom";
  public static final String DERIVATIVE_TYPE_PROP = "derivativeType";
  public static final String GENERATED_FROM_DERIVATIVE_PROP = "generatedFromDerivative";

  private Integer id;
  private ObjectStoreMetadata acDerivedFrom;
  private DerivativeType derivativeType;
  private Derivative generatedFromDerivative;

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
  @JoinColumn(name = "ac_derived_from", referencedColumnName = "id")
  public ObjectStoreMetadata getAcDerivedFrom() {
    return acDerivedFrom;
  }

  public void setAcDerivedFrom(ObjectStoreMetadata acDerivedFrom) {
    this.acDerivedFrom = acDerivedFrom;
  }

  @Type(type = "pgsql_enum")
  @Enumerated(EnumType.STRING)
  @Column(name = "derivative_type")
  public DerivativeType getDerivativeType() {
    return derivativeType;
  }

  public void setDerivativeType(DerivativeType derivativeType) {
    this.derivativeType = derivativeType;
  }

  @ManyToOne
  @JoinColumn(name = "generated_from_derivative", referencedColumnName = "id")
  public Derivative getGeneratedFromDerivative() {
    return generatedFromDerivative;
  }

  public void setGeneratedFromDerivative(Derivative generatedFromDerivative) {
    this.generatedFromDerivative = generatedFromDerivative;
  }

  @Transient
  public String getGroup() {
    return bucket;
  }
}
