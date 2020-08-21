package ca.gc.aafc.objectstore.api.entities;

import ca.gc.aafc.dina.entity.DinaEntity;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "object_upload")
@SuppressFBWarnings({ "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
@Builder(toBuilder = true)
@AllArgsConstructor
@RequiredArgsConstructor
@NaturalIdCache
public class ObjectUpload implements DinaEntity {

  private Integer id;
  private UUID fileIdentifier;
  private String createdBy;
  private OffsetDateTime createdOn;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Override
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  @NaturalId
  @NotNull
  @Column(name = "file_identifier", unique = true)
  public UUID getFileIdentifier() {
    return fileIdentifier;
  }

  public void setFileIdentifier(UUID fileIdentifier) {
    this.fileIdentifier = fileIdentifier;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  @NotBlank
  @Column(name = "created_by", updatable = false)
  public String getCreatedBy() {
    return this.createdBy;
  }

  public void setCreatedOn(OffsetDateTime createdOn) {
    this.createdOn = createdOn;
  }

  @Column(name = "created_on", insertable = false, updatable = false)
  public OffsetDateTime getCreatedOn() {
    return this.createdOn;
  }

}
