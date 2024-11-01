package ca.gc.aafc.objectstore.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Jackson Mixin used to hide relationship fields from the attribute section of JSON:API
 * when using Spring hateoas jsonapi.
 */
public abstract class DerivativeDtoMixin {

  @JsonIgnore
  private DerivativeDto generatedFromDerivative;

  @JsonIgnore
  private ObjectStoreMetadataDto acDerivedFrom;

}
