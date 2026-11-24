package ca.gc.aafc.objectstore.api.file;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;

import ca.gc.aafc.objectstore.api.config.ExternalResourceConfiguration;

/**
 * Resolves a {@code resourceExternalURL} to a file using the local file system, based on the
 * configured mounts ({@code dina.fileStorage.externalResourceMounts}).
 * <p>
 * A mount maps the mount point recorded in {@code resourceExternalURL} (stored) to where it is
 * actually mounted (active). For example, with {@code /gpsc5/abc -> /mnt/gpsc/abc}, the URL
 * {@code file:///gpsc5/abc/images/a.tif} is resolved to {@code /mnt/gpsc/abc/images/a.tif}.
 * <p>
 * Only {@code file:} URLs are supported. The resolved path must stay inside the active mount point,
 * otherwise a {@link SecurityException} is thrown (e.g. {@code file:///gpsc5/abc/../../etc/hosts}).
 */
@Log4j2
@Service
public class ExternalResourceFileResolver {

  private static final String FILE_SCHEME = "file";

  private final List<Mount> mounts;

  public ExternalResourceFileResolver(ExternalResourceConfiguration externalResourceConfiguration) {
    // longest stored mount point (most path elements) first
    this.mounts = externalResourceConfiguration.externalResourceMounts().entrySet().stream()
      .map(e -> new Mount(normalizeAbsolutePath(e.getKey()), normalizeAbsolutePath(e.getValue())))
      .sorted(Comparator.comparingInt((Mount m) -> m.storedMountPoint().getNameCount()).reversed())
      .toList();
  }

  /**
   * Resolve a resourceExternalURL to a path inside the active mount point.
   * <p>
   * An empty optional is returned when the URL is not a {@code file} URL or does not start with
   * one of the stored mount points. If the resource does not exist, the resolved path is still
   * returned so callers can report it as missing.
   *
   * @param resourceExternalURL the resourceExternalURL to resolve
   * @return the resolved path, if any
   * @throws SecurityException if the URL resolves outside the active mount point
   */
  public Optional<Path> resolve(String resourceExternalURL) {
    Path requestedPath = toFilePath(resourceExternalURL);
    if (requestedPath == null) {
      return Optional.empty();
    }

    Optional<Mount> mount = findMount(requestedPath);
    if (mount.isEmpty()) {
      log.warn("Rejected external resource outside of the configured mount points: {}",
        resourceExternalURL);
      return Optional.empty();
    }

    return Optional.of(resolveUnderActiveMountPoint(requestedPath, mount.get()));
  }

  /**
   * @param requestedPath path from the resourceExternalURL (e.g. /gpsc5/abc/images/a.tif)
   * @return the mount whose stored mount point the path starts with (e.g. /gpsc5/abc), if any
   */
  private Optional<Mount> findMount(Path requestedPath) {
    return mounts.stream()
      .filter(m -> requestedPath.startsWith(m.storedMountPoint()))
      .findFirst();
  }

  /**
   * Move the requested path from the stored mount point to the active mount point and make sure
   * it stays inside the active mount point, including once symbolic links are resolved.
   *
   * @return the real path, or the normalized path if the resource doesn't exist
   */
  private static Path resolveUnderActiveMountPoint(Path requestedPath, Mount mount) {
    Path activeMountPoint = mount.activeMountPoint();

    // e.g. /gpsc5/abc/images/a.tif relative to /gpsc5/abc -> images/a.tif.
    // requestedPath is not normalized, so ".." elements are kept and checked below.
    Path pathInsideMount = mount.storedMountPoint().relativize(requestedPath);
    Path resolvedPath = activeMountPoint.resolve(pathInsideMount).normalize();

    if (!resolvedPath.startsWith(activeMountPoint)) {
      throw pathTraversal(requestedPath);
    }

    try {
      Path realPath = resolvedPath.toRealPath();
      if (!realPath.startsWith(activeMountPoint.toRealPath())) {
        throw pathTraversal(requestedPath);
      }
      return realPath;
    } catch (IOException e) {
      // the resource doesn't exist, callers will report it as missing
      return resolvedPath;
    }
  }

  private static SecurityException pathTraversal(Path requestedPath) {
    String msg = "Path traversal detected for external resource: " + requestedPath;
    log.warn(msg);
    return new SecurityException(msg);
  }

  /**
   * @return the path of a file URL as provided (not normalized), or null if it's not a valid
   *     file URL
   */
  private static Path toFilePath(String resourceExternalURL) {
    if (StringUtils.isBlank(resourceExternalURL)) {
      return null;
    }

    try {
      URI uri = URI.create(resourceExternalURL);
      if (!FILE_SCHEME.equalsIgnoreCase(uri.getScheme())) {
        return null;
      }
      return Paths.get(uri);
    } catch (IllegalArgumentException | FileSystemNotFoundException e) {
      log.warn("Invalid resourceExternalURL: {}", resourceExternalURL);
      return null;
    }
  }

  /**
   * @param path a configured mount point (e.g. /gpsc5/abc/)
   * @return the path normalized (e.g. /gpsc5/abc)
   * @throws IllegalArgumentException if the path is not absolute
   */
  private static Path normalizeAbsolutePath(String path) {
    Path parsedPath = Paths.get(path);
    if (!parsedPath.isAbsolute()) {
      throw new IllegalArgumentException(
        "externalResourceMounts entries must be absolute paths: " + path);
    }
    return parsedPath.normalize();
  }

  /**
   * Stored (recorded in resourceExternalURL) to active (actually mounted) mount point.
   */
  private record Mount(Path storedMountPoint, Path activeMountPoint) {
  }
}
