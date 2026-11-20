package ca.gc.aafc.objectstore.api.file;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
  private static final String PATH_TRAVERSAL_MSG = "Path traversal detected";

  private final List<Mount> mounts;

  public ExternalResourceFileResolver(ExternalResourceConfiguration externalResourceConfiguration) {
    List<Mount> configuredMounts = new ArrayList<>();
    for (Map.Entry<String, String> entry : externalResourceConfiguration.externalResourceMounts()
        .entrySet()) {
      Path storedMountPoint = toMountPoint(entry.getKey());
      Path activeMountPoint = toMountPoint(entry.getValue());
      configuredMounts.add(new Mount(storedMountPoint, activeMountPoint));
    }

    // longest stored mount point (most path elements) first
    configuredMounts.sort((a, b) -> Integer.compare(
      b.storedMountPoint().getNameCount(), a.storedMountPoint().getNameCount()));

    this.mounts = List.copyOf(configuredMounts);
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

    try {
      return Optional.of(resolveUnderActiveMountPoint(requestedPath, mount.get()));
    } catch (SecurityException e) {
      log.warn("{} for external resource: {}", PATH_TRAVERSAL_MSG, resourceExternalURL);
      throw e;
    }
  }

  /**
   * Find the mount whose stored mount point the requested path starts with.
   *
   * @param requestedPath path from the resourceExternalURL (e.g. /gpsc5/abc/images/a.tif)
   * @return the matching mount (e.g. /gpsc5/abc -> /mnt/gpsc/abc), if any
   */
  private Optional<Mount> findMount(Path requestedPath) {
    for (Mount mount : mounts) {
      if (requestedPath.startsWith(mount.storedMountPoint())) {
        return Optional.of(mount);
      }
    }
    return Optional.empty();
  }

  /**
   * Move the requested path from the stored mount point to the active mount point and make sure
   * it stays inside the active mount point.
   */
  private static Path resolveUnderActiveMountPoint(Path requestedPath, Mount mount) {
    Path activeMountPoint = mount.activeMountPoint();

    Path pathInsideMount = removeMountPoint(requestedPath, mount.storedMountPoint());

    Path resolvedPath = activeMountPoint.resolve(pathInsideMount).normalize();

    if (!resolvedPath.startsWith(activeMountPoint)) {
      throw new SecurityException(PATH_TRAVERSAL_MSG);
    }

    return checkRealPathInsideMountPoint(resolvedPath, activeMountPoint);
  }

  /**
   * Remove the mount point from the beginning of the path. The result is kept as provided
   * (not normalized) so ".." elements are still there to be checked.
   *
   * @param path       e.g. /gpsc5/abc/images/a.tif
   * @param mountPoint e.g. /gpsc5/abc
   * @return e.g. images/a.tif, or an empty path if path is the mount point itself
   */
  private static Path removeMountPoint(Path path, Path mountPoint) {
    int start = mountPoint.getNameCount();
    int end = path.getNameCount();
    if (start == end) {
      return Paths.get("");
    }
    return path.subpath(start, end);
  }

  /**
   * Make sure the resource does not escape the active mount point through a symbolic link.
   *
   * @return the real path, or the resolved path if the resource doesn't exist
   */
  private static Path checkRealPathInsideMountPoint(Path resolvedPath, Path activeMountPoint) {
    Path realPath;
    Path realMountPoint;
    try {
      realPath = resolvedPath.toRealPath();
      realMountPoint = activeMountPoint.toRealPath();
    } catch (IOException e) {
      // the resource doesn't exist, callers will report it as missing
      return resolvedPath;
    }

    if (!realPath.startsWith(realMountPoint)) {
      throw new SecurityException(PATH_TRAVERSAL_MSG);
    }
    return realPath;
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

  private static Path toMountPoint(String mountPoint) {
    Path path = Paths.get(mountPoint);
    if (!path.isAbsolute()) {
      throw new IllegalArgumentException(
        "externalResourceMounts entries must be absolute paths: " + mountPoint);
    }
    return path.normalize();
  }

  /**
   * @param storedMountPoint mount point recorded in resourceExternalURL (e.g. /gpsc5/abc)
   * @param activeMountPoint where it is actually mounted (e.g. /mnt/gpsc/abc)
   */
  private record Mount(Path storedMountPoint, Path activeMountPoint) {
  }
}
