package ca.gc.aafc.objectstore.api;

import ca.gc.aafc.dina.repository.external.ExternalResourceProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Set;

@Configuration
@Getter
@Setter
@RequiredArgsConstructor
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "external-types")
public class ObjectStoreExternalResourceProvider implements ExternalResourceProvider {

  private final Map<String, String> providerMap;

  @Override
  public String getReferenceForType(String type) {
    if (!providerMap.containsKey(type)) {
      throw new IllegalArgumentException(
        "No external type of [" + type + "] is defined by the ExternalResourceProvider");
    }
    return providerMap.get(type);
  }

  @Override
  public Set<String> getTypes() {
    return providerMap.keySet();
  }
}
