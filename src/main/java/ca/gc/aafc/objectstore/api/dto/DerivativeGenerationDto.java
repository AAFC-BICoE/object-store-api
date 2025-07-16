package ca.gc.aafc.objectstore.api.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.toedter.spring.hateoas.jsonapi.JsonApiId;
import com.toedter.spring.hateoas.jsonapi.JsonApiTypeForClass;

import ca.gc.aafc.objectstore.api.entities.Derivative;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonApiTypeForClass(DerivativeGenerationDto.TYPENAME)
public class DerivativeGenerationDto implements ca.gc.aafc.dina.dto.JsonApiResource {

  public static final String TYPENAME = "derivative-generation";

  @JsonApiId
  private UUID uuid;

  private UUID metadataUuid;
  private Derivative.DerivativeType derivativeType;

  private Derivative.DerivativeType derivedFromType;

  @Override
  @JsonIgnore
  public String getJsonApiType() {
    return TYPENAME;
  }

  @Override
  @JsonIgnore
  public UUID getJsonApiId() {
    return uuid;
  }
}
