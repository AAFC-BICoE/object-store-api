package ca.gc.aafc.objectstore.api;

import ca.gc.aafc.dina.repository.external.ExternalResourceProvider;
import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class ObjectStoreExternalResourceProvider implements ExternalResourceProvider {

  public static final Map<String, String> TYPE_TO_REFERENCE_MAP = ImmutableMap.of(
    "person", "Agent/api/v1/person");

  @Override
  public String getReferenceForType(String type) {
    if (!TYPE_TO_REFERENCE_MAP.containsKey(type)) {
      throw new IllegalArgumentException(
        "No external type of [" + type + "] is is defined by the ExternalResourceProvider");
    }
    return TYPE_TO_REFERENCE_MAP.get(type);
  }

  @Override
  public Set<String> getTypes() {
    return TYPE_TO_REFERENCE_MAP.keySet();
  }
}
