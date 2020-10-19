package ca.gc.aafc.objectstore.api;

import ca.gc.aafc.dina.repository.external.ExternalResourceProvider;
import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class ObjectStoreExternalResourceProvider implements ExternalResourceProvider {

  public static final Map<String, String> typeToReferenceMap = ImmutableMap.of(
    "person", "Agent/api/v1/person");

  @Override
  public String getReferenceForType(String type) {
    if (!typeToReferenceMap.containsKey(type)) {
      throw new IllegalArgumentException(
        "No external type of [" + type + "] is is defined by the ExternalResourceProvider");
    }
    return typeToReferenceMap.get(type);
  }

  @Override
  public Set<String> getTypes() {
    return typeToReferenceMap.keySet();
  }
}
