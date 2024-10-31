package ca.gc.aafc.objectstore.api.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class ObjectStoreMetadataDtoMixin {

  @JsonIgnore
  List<DerivativeDto> derivatives;


}
