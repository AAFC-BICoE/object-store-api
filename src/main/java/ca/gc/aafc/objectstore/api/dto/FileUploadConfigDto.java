package ca.gc.aafc.objectstore.api.dto;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonApiResource(type = "file-upload-config")
public class FileUploadConfigDto {
  @JsonApiId
  private String id;
  private String maxFileSize;
  private String maxRequestSize;
}
