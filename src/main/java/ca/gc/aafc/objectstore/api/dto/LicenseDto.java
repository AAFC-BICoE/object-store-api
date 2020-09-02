package ca.gc.aafc.objectstore.api.dto;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.Data;

import java.util.Map;

@Data
@JsonApiResource(type = "license")
public class LicenseDto {

  @JsonApiId
  private String id;

  private String url;

  private Map<String, String> titles;

}
