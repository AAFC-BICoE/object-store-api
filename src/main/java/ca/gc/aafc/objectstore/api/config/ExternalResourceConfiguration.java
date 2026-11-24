package ca.gc.aafc.objectstore.api.config;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration of externally hosted resources that can be read from the local file system.
 *
 * @param externalResourceMounts mount points of external resources: the key is the mount point
 *                               recorded in {@code resourceExternalURL} (stored), the value is
 *                               where it is actually mounted in the context of the container/pod
 *                               (active). Null or empty means no external resource can be resolved.
 */
@ConfigurationProperties(prefix = "dina.file-storage")
public record ExternalResourceConfiguration(Map<String, String> externalResourceMounts) {

  public ExternalResourceConfiguration {
    externalResourceMounts = externalResourceMounts == null
      ? Map.of() : Map.copyOf(externalResourceMounts);
  }
}
