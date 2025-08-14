package ca.gc.aafc.objectstore.api.entities;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.URL;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The Class ObjectStoreMetadata.
 */
@Entity
@Table(name = "metadata")
@RequiredArgsConstructor
public class ObjectStoreMetadata extends AbstractObjectStoreMetadata {

  public static final String DERIVATIVES_PROP = "derivatives";

  private Integer id;

  private String acCaption;

  private OffsetDateTime acDigitizationDate;
  private OffsetDateTime xmpMetadataDate;

  private String xmpRightsWebStatement;
  private String dcRights;
  private String xmpRightsOwner;
  private String xmpRightsUsageTerms;

  private String originalFilename;

  private Map<String, String> managedAttributes;

  private UUID acMetadataCreator;
  private UUID dcCreator;

  private List<Derivative> derivatives;

  private ObjectSubtype acSubtype;

  private Integer orientation;

  private String resourceExternalURL;

  /**
   * Read-only field to get the ac_sub_type_id to allow filtering by null values.
   */
  private Integer acSubtypeId;

  @Builder
  public ObjectStoreMetadata(
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
      String dcFormat,
      String acCaption,
      OffsetDateTime acDigitizationDate,
      OffsetDateTime xmpMetadataDate,
      String xmpRightsWebStatement,
      String dcRights,
      String xmpRightsOwner,
      String xmpRightsUsageTerms,
      String originalFilename,
      String[] acTags,
      Map<String, String> managedAttributes,
      UUID acMetadataCreator,
      UUID dcCreator,
      List<Derivative> derivatives,
      Boolean publiclyReleasable,
      String notPubliclyReleasableReason,
      ObjectSubtype acSubtype,
      Integer acSubtypeId,
      Integer orientation,
      String resourceExternalURL) {
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
        dcFormat,
        publiclyReleasable,
        notPubliclyReleasableReason,
        acTags);
    this.id = id;
    this.acCaption = acCaption;
    this.acDigitizationDate = acDigitizationDate;
    this.xmpMetadataDate = xmpMetadataDate;
    this.xmpRightsWebStatement = xmpRightsWebStatement;
    this.dcRights = dcRights;
    this.xmpRightsOwner = xmpRightsOwner;
    this.xmpRightsUsageTerms = xmpRightsUsageTerms;
    this.originalFilename = originalFilename;
    this.managedAttributes = MapUtils.isNotEmpty(managedAttributes) ? managedAttributes : new HashMap<>();
    this.acMetadataCreator = acMetadataCreator;
    this.dcCreator = dcCreator;
    this.derivatives = CollectionUtils.isNotEmpty(derivatives) ? derivatives : new ArrayList<>();
    this.acSubtype = acSubtype;
    this.acSubtypeId = acSubtypeId;
    this.orientation = orientation;
    this.resourceExternalURL = resourceExternalURL;
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

  @Column(name = "ac_caption")
  @Size(max = 250)
  public String getAcCaption() {
    return acCaption;
  }

  public void setAcCaption(String acCaption) {
    this.acCaption = acCaption;
  }

  @Column(name = "ac_digitization_date")
  public OffsetDateTime getAcDigitizationDate() {
    return acDigitizationDate;
  }

  public void setAcDigitizationDate(OffsetDateTime acDigitizationDate) {
    this.acDigitizationDate = acDigitizationDate;
  }

  @UpdateTimestamp
  @Column(name = "xmp_metadata_date")
  public OffsetDateTime getXmpMetadataDate() {
    return xmpMetadataDate;
  }

  public void setXmpMetadataDate(OffsetDateTime xmpMetadataDate) {
    this.xmpMetadataDate = xmpMetadataDate;
  }

  @Column(name = "original_filename")
  public String getOriginalFilename() {
    return originalFilename;
  }

  public void setOriginalFilename(String originalFilename) {
    this.originalFilename = originalFilename;
  }

  @NotNull
  @Column(name = "xmp_rights_web_statement")
  @Size(max = 250)
  public String getXmpRightsWebStatement() {
    return xmpRightsWebStatement;
  }

  public void setXmpRightsWebStatement(String xmpRightsWebStatement) {
    this.xmpRightsWebStatement = xmpRightsWebStatement;
  }

  @NotNull
  @Column(name = "ac_rights")
  @Size(max = 250)
  public String getDcRights() {
    return dcRights;
  }

  public void setDcRights(String dcRights) {
    this.dcRights = dcRights;
  }

  @NotNull
  @Column(name = "xmp_rights_owner")
  @Size(max = 250)
  public String getXmpRightsOwner() {
    return xmpRightsOwner;
  }

  public void setXmpRightsOwner(String xmpRightsOwner) {
    this.xmpRightsOwner = xmpRightsOwner;
  }

  @OneToMany(mappedBy = "acDerivedFrom", cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
  public List<Derivative> getDerivatives() {
    return derivatives;
  }

  public void setDerivatives(List<Derivative> derivatives) {
    this.derivatives = derivatives;
  }

  /**
   * Adds the given derivative to the list of derivatives. This method should be
   * used to establish Bi-directional JPA relations ships.
   *
   * @param derivative - derivative to add
   */
  public void addDerivative(Derivative derivative) {
    derivatives.add(derivative);
    derivative.setAcDerivedFrom(this);
  }

  /**
   * Adds the given derivative to the list of derivatives. This method should be
   * used to remove Bi directional JPA relations ships.
   *
   * @param derivative - derivative to remove
   */
  public void removeDerivative(Derivative derivative) {
    derivatives.remove(derivative);
    derivative.setAcDerivedFrom(null);
  }

  @ManyToOne
  @JoinColumn(name = "ac_sub_type_id", referencedColumnName = "id")
  public ObjectSubtype getAcSubtype() {
    return acSubtype;
  }

  public void setAcSubtype(ObjectSubtype acSubtype) {
    this.acSubtype = acSubtype;
  }

  /**
   * Read-only field to get the ac_derived_from_id to allow filtering by null
   * values.
   */
  @Column(name = "ac_sub_type_id", updatable = false, insertable = false)
  public Integer getAcSubtypeId() {
    return acSubtypeId;
  }

  public void setAcSubtypeId(Integer acSubtypeId) {
    this.acSubtypeId = acSubtypeId;
  }

  @Column(name = "ac_metadata_creator_id")
  public UUID getAcMetadataCreator() {
    return acMetadataCreator;
  }

  public void setAcMetadataCreator(UUID acMetadataCreator) {
    this.acMetadataCreator = acMetadataCreator;
  }

  @Column(name = "dc_creator_id")
  public UUID getDcCreator() {
    return dcCreator;
  }

  public void setDcCreator(UUID dcCreator) {
    this.dcCreator = dcCreator;
  }

  @NotNull
  @Column(name = "xmp_rights_usage_terms")
  @Size(max = 500)
  public String getXmpRightsUsageTerms() {
    return xmpRightsUsageTerms;
  }

  public void setXmpRightsUsageTerms(String xmpRightsUsageTerms) {
    this.xmpRightsUsageTerms = xmpRightsUsageTerms;
  }

  @Min(value = 1)
  @Max(value = 8)
  public Integer getOrientation() {
    return orientation;
  }

  public void setOrientation(Integer orientation) {
    this.orientation = orientation;
  }

  /**
   * Transient field until base implementation is ready
   **/
  @Transient
  public String getGroup() {
    return bucket;
  }

  /**
   * Empty setter method to avoid resource method error: missing accessor method
   * until multiple custom field resolvers can be associated with single resource
   */
  public void setGroup(String group) {
  }

  @Type(type = "jsonb")
  @NotNull
  @Column(name = "managed_attribute_values")
  public Map<String, String> getManagedAttributes() {
    return managedAttributes;
  }

  public void setManagedAttributes(Map<String, String> managedAttributes) {
    this.managedAttributes = managedAttributes;
  }

  @Transient
  public boolean isExternal() {
    return StringUtils.isNotBlank(resourceExternalURL);
  }

  @Column(name = "resource_external_url")
  @URL
  @Size(max = 255)
  public String getResourceExternalURL() {
    return resourceExternalURL;
  }

  public void setResourceExternalURL(String resourceExternalURL) {
    this.resourceExternalURL = resourceExternalURL;
  }
}
