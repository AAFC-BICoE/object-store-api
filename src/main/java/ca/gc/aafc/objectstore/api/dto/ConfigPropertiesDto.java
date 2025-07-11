package ca.gc.aafc.objectstore.api.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.toedter.spring.hateoas.jsonapi.JsonApiId;
import com.toedter.spring.hateoas.jsonapi.JsonApiTypeForClass;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Builder
@JsonApiTypeForClass(ConfigPropertiesDto.TYPENAME)
public class ConfigPropertiesDto {

  public static final String TYPENAME = "config";

  @JsonApiId
  @Getter
  @Setter
  private String id;

  @JsonIgnore
  private Map<String, Object> properties;

  @JsonAnyGetter
  public Map<String, Object> getProperties() {
    return properties;
  }

  @JsonAnySetter
  public void setProperties(String propertyName, Object propertyValue) {
    if (properties == null) {
      properties = new HashMap<>();
    }
    this.properties.put(propertyName, propertyValue);
  }
}
