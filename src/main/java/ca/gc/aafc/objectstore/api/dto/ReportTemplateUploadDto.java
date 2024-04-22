package ca.gc.aafc.objectstore.api.dto;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonApiResource(type = ReportTemplateUploadDto.TYPENAME)
public class ReportTemplateUploadDto {

  public static final String TYPENAME = "report-template-upload";

  @JsonApiId
  private UUID uuid;

  private UUID fileIdentifier;

}
