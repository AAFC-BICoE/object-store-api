package ca.gc.aafc.objectstore.api;

import javax.inject.Inject;

import org.springframework.context.annotation.Configuration;

import ca.gc.aafc.objectstore.api.dto.ObjectStoreManagedAttributeDto;
import ca.gc.aafc.objectstore.api.util.ManagedAttributeIdMapper;
import io.crnk.core.engine.registry.ResourceRegistry;

@Configuration
public class ManagedAttributeConfiguration {
  
  @Inject
  @SuppressWarnings({"deprecation", "unchecked"})
  public void setupManagedAttributeLookup(ResourceRegistry resourceRegistry) {
    var resourceInfo = resourceRegistry.getEntry(ObjectStoreManagedAttributeDto.class)
      .getResourceInformation();

    resourceInfo.setIdStringMapper(
      new ManagedAttributeIdMapper(resourceInfo.getIdStringMapper()));
  }

}
