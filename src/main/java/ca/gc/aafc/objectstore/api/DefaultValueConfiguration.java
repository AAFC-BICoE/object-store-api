package ca.gc.aafc.objectstore.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "default-values")
@Getter
@Setter
public class DefaultValueConfiguration {

  private List<DefaultValue> values;

  public Map<String, Object> getProperties(){
    return Map.copyOf(Map.of("values", values));
  }

  @ConstructorBinding
  @RequiredArgsConstructor
  @Getter
  public static class DefaultValue {
    private final String type;
    private final String attribute;
    private final String value;
  }

}
