package ca.gc.aafc.objectstore.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.List;

@ConfigurationProperties(prefix = "default-values")
@Getter
@Setter
public class DefaultValueConfiguration {

  private List<DefaultValue> values;

  @ConstructorBinding
  @RequiredArgsConstructor
  @Getter
  public static class DefaultValue {
    private final String type;
    private final String attribute;
    private final String value;
  }

}
