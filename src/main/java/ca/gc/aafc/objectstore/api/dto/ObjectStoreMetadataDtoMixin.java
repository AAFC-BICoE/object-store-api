package ca.gc.aafc.objectstore.api.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ca.gc.aafc.dina.dto.ExternalRelationDto;

/**
 * Jackson Mixin used to hide relationship fields from the attribute section of JSON:API
 * when using Spring hateoas jsonapi.
 */
public abstract class ObjectStoreMetadataDtoMixin {

  @JsonIgnore
  private List<DerivativeDto> derivatives;

  @JsonIgnore
  private ExternalRelationDto acMetadataCreator;

  @JsonIgnore
  private ExternalRelationDto dcCreator;

}
