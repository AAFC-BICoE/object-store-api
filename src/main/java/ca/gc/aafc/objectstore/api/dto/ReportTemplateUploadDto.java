package ca.gc.aafc.objectstore.api.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.toedter.spring.hateoas.jsonapi.JsonApiId;
import com.toedter.spring.hateoas.jsonapi.JsonApiTypeForClass;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonApiTypeForClass(ReportTemplateUploadDto.TYPENAME)
public class ReportTemplateUploadDto implements ca.gc.aafc.dina.dto.JsonApiResource {

  public static final String TYPENAME = "report-template-upload";

  @JsonApiId
  private UUID uuid;

  private UUID fileIdentifier;

  @Override
  public String getJsonApiType() {
    return TYPENAME;
  }

  @Override
  public UUID getJsonApiId() {
    return uuid;
  }
}
