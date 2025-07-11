package ca.gc.aafc.objectstore.api.dto;

import lombok.Data;

import java.util.Map;

import com.toedter.spring.hateoas.jsonapi.JsonApiId;
import com.toedter.spring.hateoas.jsonapi.JsonApiTypeForClass;

@Data
@JsonApiTypeForClass(LicenseDto.TYPENAME)
public class LicenseDto {

  public static final String TYPENAME = "license";

  @JsonApiId
  private String id;

  private String url;

  private Map<String, String> titles;

}
