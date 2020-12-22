package ca.gc.aafc.objectstore.api.dto;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonApiResource(type = "default-values")
public class DefaultValuesDto {
  @JsonApiId
  private Integer id;
  private final String type;
  private final String attribute;
  private final String value;
}
