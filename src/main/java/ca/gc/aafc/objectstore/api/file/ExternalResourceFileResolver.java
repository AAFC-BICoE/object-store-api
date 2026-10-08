package ca.gc.aafc.objectstore.api.file;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class ExternalResourceFileResolver {

  private final Path externalResourceBasePath;

  public ExternalResourceFileResolver(
    @Value("${dina.fileStorage.externalResourceBasePath:}") String externalResourceBasePath
  ) {
    this.externalResourceBasePath = StringUtils.isBlank(externalResourceBasePath)
      ? null : Paths.get(externalResourceBasePath).toAbsolutePath().normalize();
  }

  /**
   * Resolve a resourceExternalURL to a path inside the configured external resource base path.
   */
  public Optional<Path> resolve(String resourceExternalURL) {
    if (externalResourceBasePath == null || StringUtils.isBlank(resourceExternalURL)) {
      return Optional.empty();
    }
    try {
      Path path = Paths.get(URI.create(resourceExternalURL)).normalize();
      if (!path.startsWith(externalResourceBasePath)) {
        log.warn("Rejected external resource outside of the configured base path: {}",
          resourceExternalURL);
        return Optional.empty();
      }
      return Optional.of(path);
    } catch (IllegalArgumentException e) {
      log.warn("Invalid resourceExternalURL: {}", resourceExternalURL);
      return Optional.empty();
    }
  }
}
