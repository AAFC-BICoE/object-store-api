package ca.gc.aafc.objectstore.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import ca.gc.aafc.dina.dto.RelatedEntity;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.Data;


@SuppressFBWarnings({ "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
@RelatedEntity(ObjectUpload.class)
@Data
@JsonApiResource(type = ObjectUploadDto.TYPENAME)
public class ObjectUploadDto {

  public static final String TYPENAME = "object-upload";
  
  @JsonApiId
  private UUID fileIdentifier;

  private String createdBy;
  private OffsetDateTime createdOn;

}
