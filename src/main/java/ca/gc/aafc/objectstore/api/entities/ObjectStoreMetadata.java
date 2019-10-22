package ca.gc.aafc.objectstore.api.entities;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;

import ca.gc.aafc.objectstore.api.interfaces.UniqueObj;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

/**
 * The Class ObjectStoreMetadata.
 */

@Entity
@Table(name = "metadata")
@TypeDef(name = "pgsql_enum", typeClass = PostgreSQLEnumType.class)
@SuppressFBWarnings({ "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@NaturalIdCache
public class ObjectStoreMetadata implements java.io.Serializable, UniqueObj {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -5655824300348079540L;

  private Integer id;

  private UUID uuid;

  private String dcFormat;
  private DcType dcType;

  private OffsetDateTime acDigitizationDate;
  private OffsetDateTime xmpMetadataDate;

  private String acHashFunction;
  private String acHashValue;
  
  private List<MetadataManagedAttribute> managedAttribute;  

  public enum DcType {
    IMAGE("Image"), 
    MOVING_IMAGE("Moving Image"), 
    SOUND("Sound"), 
    TEXT("Text");

    private final String value;

    DcType(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    /**
     * Get the {@link DcType} value from the provided string. The string is matched in a case
     * insensitive manner.
     * 
     * @param value
     * @return the {@link DcType} wrapped in an {@link Optional} or {@link Optional#empty()} is no
     *         there is {@link DcType} match.
     */
    public static Optional<DcType> fromValue(String value) {
      for (DcType currType : values()) {
        if (currType.getValue().equalsIgnoreCase(value)) {
          return Optional.of(currType);
        }
      }
      return Optional.empty();
    }
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

  @NaturalId
  @NotNull
  @Column(name = "uuid", unique = true)
  public UUID getUuid() {
    return uuid;
  }

  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }

  @Column(name = "dc_format")
  public String getDcFormat() {
    return dcFormat;
  }

  public void setDcFormat(String dcFormat) {
    this.dcFormat = dcFormat;
  }

  @NotNull
  @Type(type = "pgsql_enum")
  @Enumerated(EnumType.STRING)
  @Column(name = "dc_type")
  public DcType getDcType() {
    return dcType;
  }

  public void setDcType(DcType dcType) {
    this.dcType = dcType;
  }

  @Column(name = "ac_digitization_date")
  public OffsetDateTime getAcDigitizationDate() {
    return acDigitizationDate;
  }

  public void setAcDigitizationDate(OffsetDateTime acDigitizationDate) {
    this.acDigitizationDate = acDigitizationDate;
  }

  @Column(name = "xmp_metadata_date")
  public OffsetDateTime getXmpMetadataDate() {
    return xmpMetadataDate;
  }

  public void setXmpMetadataDate(OffsetDateTime xmpMetadataDate) {
    this.xmpMetadataDate = xmpMetadataDate;
  }

  @Column(name = "ac_hash_function")
  public String getAcHashFunction() {
    return acHashFunction;
  }

  public void setAcHashFunction(String acHashFunction) {
    this.acHashFunction = acHashFunction;
  }

  @Column(name = "ac_hash_value")
  public String getAcHashValue() {
    return acHashValue;
  }

  public void setAcHashValue(String acHashValue) {
    this.acHashValue = acHashValue;
  }

  @OneToMany
  @JoinColumn(name = "metadata_id")
  public List<MetadataManagedAttribute> getManagedAttribute() {
    return managedAttribute;
  }

  public void setManagedAttribute(List<MetadataManagedAttribute> managedAttribute) {
    this.managedAttribute = managedAttribute;
  }

}
