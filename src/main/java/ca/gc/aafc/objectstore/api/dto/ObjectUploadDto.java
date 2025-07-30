package ca.gc.aafc.objectstore.api.dto;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import ca.gc.aafc.dina.dto.JsonApiResource;
import ca.gc.aafc.dina.dto.RelatedEntity;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import lombok.Data;
import org.javers.core.metamodel.annotation.ShallowReference;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.toedter.spring.hateoas.jsonapi.JsonApiId;
import com.toedter.spring.hateoas.jsonapi.JsonApiTypeForClass;

@RelatedEntity(ObjectUpload.class)
@Data
@JsonApiTypeForClass(ObjectSubtypeDto.TYPENAME)
public class ObjectUploadDto implements JsonApiResource {

  public static final String TYPENAME = "object-upload";

  @JsonApiId
  private UUID fileIdentifier;

  @ShallowReference
  private DcType dcType;

  private String createdBy;
  private OffsetDateTime createdOn;
  private String originalFilename;
  private String sha1Hex;
  private String receivedMediaType;
  private String detectedMediaType;
  private String detectedFileExtension;
  private String evaluatedMediaType;
  private String evaluatedFileExtension;
  private long sizeInBytes;
  private String bucket;
  private String dateTimeDigitized;
  private Map<String, String> exif;
  private boolean isDerivative;

  public boolean getIsDerivative() {
    return isDerivative;
  }

  public void setIsDerivative(boolean derivative) {
    isDerivative = derivative;
  }

  @Override
  @JsonIgnore
  public String getJsonApiType() {
    return TYPENAME;
  }

  @Override
  @JsonIgnore
  public UUID getJsonApiId() {
    return fileIdentifier;
  }
}
