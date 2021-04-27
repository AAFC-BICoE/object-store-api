package ca.gc.aafc.objectstore.api.entities;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

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
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "derivative")
@NoArgsConstructor
@AllArgsConstructor
@TypeDef(name = "pgsql_enum", typeClass = PostgreSQLEnumType.class)
public class Derivative extends AbstractObjectStoreMetadata {

  public enum DerivativeType {
    THUMBNAIL_IMAGE, LARGE_IMAGE
  }

  private Integer id;
  private ObjectStoreMetadata acDerivedFrom;
  private DerivativeType derivativeType;
  private Derivative generatedFromDerivative;

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
    ObjectStoreMetadata acDerivedFrom,
    DerivativeType derivativeType,
    Derivative generatedFromDerivative,
    String dcFormat
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
      createdOn,
      dcFormat);
    this.id = id;
    this.acDerivedFrom = acDerivedFrom;
    this.derivativeType = derivativeType;
    this.generatedFromDerivative = generatedFromDerivative;
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
}
