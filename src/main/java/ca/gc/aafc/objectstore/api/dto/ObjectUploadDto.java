package ca.gc.aafc.objectstore.api.dto;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import ca.gc.aafc.dina.dto.RelatedEntity;
import ca.gc.aafc.dina.repository.meta.AttributeMetaInfoProvider;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.Data;
import org.javers.core.metamodel.annotation.ShallowReference;

@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
@RelatedEntity(ObjectUpload.class)
@Data
@JsonApiResource(type = ObjectUploadDto.TYPENAME)
public class ObjectUploadDto extends AttributeMetaInfoProvider {

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

}
