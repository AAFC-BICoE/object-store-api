package ca.gc.aafc.objectstore.api.dto;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.Data;

@Data
@JsonApiResource(type = "default-values")
public class DefaultValuesDto {
  @JsonApiId
  private Integer id;
  private String defaultLicenceURL;
  private String defaultCopyright;
  private String defaultCopyrightOwner;
  private String defaultUsageTerms;
}
