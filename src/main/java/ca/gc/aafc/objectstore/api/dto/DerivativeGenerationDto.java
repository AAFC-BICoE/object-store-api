package ca.gc.aafc.objectstore.api.dto;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import ca.gc.aafc.objectstore.api.entities.Derivative;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonApiResource(type = DerivativeGenerationDto.TYPENAME)
public class DerivativeGenerationDto {

  public static final String TYPENAME = "derivative-generation";

  @JsonApiId
  private UUID uuid;

  private UUID metadataUuid;
  private Derivative.DerivativeType derivativeType;

  private Derivative.DerivativeType derivedFromType;

}
