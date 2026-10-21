package ca.gc.aafc.objectstore.api.file;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;

/**
 * Resolves a {@code resourceExternalURL} to a file using the local file system, constrained
 * to the configured {@code externalResourceBasePath}.
 * <p>
 * Only {@code file:} URLs that resolve beneath the configured base path are considered locally
 * exportable. Website (or any other non file) URLs are not supported. Paths outside the base path,
 * or paths that escape the base path through symbolic links, are rejected.
 */
@Log4j2
@Service
public class ExternalResourceFileResolver {

  private static final String FILE_SCHEME = "file";

  private final Path externalResourceBasePath;

  public ExternalResourceFileResolver(
    @Value("${dina.fileStorage.externalResourceBasePath:}") String externalResourceBasePath
  ) {
    this.externalResourceBasePath = StringUtils.isBlank(externalResourceBasePath)
      ? null : toRealPathOrAbsoluteNormalized(Paths.get(externalResourceBasePath));
  }

  /**
   * @param resourceExternal the resourceExternal URI to inspect, may be null or blank
   * @return true if the given resourceExternal URI has a {@code file} scheme, false otherwise
   */
  private static boolean isFileScheme(URI resourceExternal) {
    return FILE_SCHEME.equalsIgnoreCase(resourceExternal.getScheme( ));
  }

  /**
   * Resolve a resourceExternalURL to a path inside the configured external resource base path.
   * <p>
   * An empty optional is returned when no base path is configured, when the URL is not a
   * {@code file} URL, or when the resolved path is outside (or escapes through a symbolic link) the
   * base path.
   *
   * @param resourceExternalURL the resourceExternalURL to resolve
   * @return the resolved path, if any
   */
  public Optional<Path> resolve(String resourceExternalURL) {
    if (externalResourceBasePath == null) {
      return Optional.empty();
    }

    Path path;
    try {
      URI resourceExternal = URI.create(resourceExternalURL);

      if (!isFileScheme(resourceExternal)) {
        return Optional.empty();
      }
      path = Paths.get(resourceExternal).toAbsolutePath().normalize();
    } catch (IllegalArgumentException | FileSystemNotFoundException e) {
      log.warn("Invalid resourceExternalURL: {}", resourceExternalURL);
      return Optional.empty();
    }

    if (!path.startsWith(externalResourceBasePath)) {
      log.warn("Rejected external resource outside of the configured base path: {}",
        resourceExternalURL);
      return Optional.empty();
    }

    // make sure the resource does not escape the configured base path through a symbolic link
    try {
      Path realPath = path.toRealPath();
      if (!realPath.startsWith(externalResourceBasePath)) {
        log.warn("Rejected external resource escaping the configured base path: {}",
          resourceExternalURL);
        return Optional.empty();
      }
      return Optional.of(realPath);
    } catch (IOException e) {
      // the resource does not exist, keep the normalized path so callers can report it as missing
      return Optional.of(path);
    }
  }

  private static Path toRealPathOrAbsoluteNormalized(Path path) {
    try {
      return path.toRealPath();
    } catch (IOException e) {
      return path.toAbsolutePath().normalize();
    }
  }
}
