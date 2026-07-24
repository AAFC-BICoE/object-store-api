package ca.gc.aafc.objectstore.api.entities;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ac_derived_from", referencedColumnName = "id")
  public ObjectStoreMetadata getAcDerivedFrom() {
    return acDerivedFrom;
  }

  public void setAcDerivedFrom(ObjectStoreMetadata acDerivedFrom) {
    this.acDerivedFrom = acDerivedFrom;
  }

  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Enumerated(EnumType.STRING)
  @Column(name = "derivative_type")
  public DerivativeType getDerivativeType() {
    return derivativeType;
  }

  public void setDerivativeType(DerivativeType derivativeType) {
    this.derivativeType = derivativeType;
  }

  @ManyToOne(fetch = FetchType.LAZY)
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
