package ca.gc.aafc.objectstore.api.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ca.gc.aafc.objectstore.api.config.ExternalResourceConfiguration;

/**
 * Tests related to {@link ExternalResourceFileResolver}
 */
public class ExternalResourceFileResolverTest {

  @TempDir
  Path tempDir;

  // mount points as recorded in resourceExternalURL (they don't exist on the file system)
  private Path storedMount;
  private Path otherStoredMount;

  // where the stored mount points are actually mounted
  private Path activeMount;
  private Path otherActiveMount;

  private Path notMounted;

  @BeforeEach
  void setUp() throws IOException {
    Path root = tempDir.toRealPath();
    storedMount = root.resolve("gpsc5").resolve("abc");
    otherStoredMount = root.resolve("gpsc5").resolve("other");
    activeMount = Files.createDirectories(root.resolve("mnt").resolve("gpsc"));
    otherActiveMount = Files.createDirectories(root.resolve("mnt").resolve("other"));
    notMounted = Files.createDirectory(root.resolve("not-mounted"));
  }

  private ExternalResourceFileResolver newResolver() {
    return newResolver(Map.of(
      storedMount.toString(), activeMount.toString(),
      otherStoredMount.toString(), otherActiveMount.toString()));
  }

  private static ExternalResourceFileResolver newResolver(Map<String, String> mounts) {
    return new ExternalResourceFileResolver(new ExternalResourceConfiguration(mounts));
  }

  /**
   * Build a file URL without any normalization (URI.resolve would normalize it).
   */
  private static String fileUrl(Path storedFolder, String relative) {
    return storedFolder.toUri() + "/" + relative;
  }

  @Test
  public void resolve_whenNoMountConfigured_empty() throws IOException {
    Files.createFile(activeMount.resolve("no-mount.txt"));
    ExternalResourceFileResolver resolver =
      new ExternalResourceFileResolver(new ExternalResourceConfiguration(null));
    assertTrue(resolver.resolve(fileUrl(storedMount, "no-mount.txt")).isEmpty());
  }

  @Test
  public void resolve_whenFileUnderStoredMount_activePathReturned() throws IOException {
    Path file = Files.createFile(activeMount.resolve("inside.txt"));

    Optional<Path> resolved = newResolver().resolve(fileUrl(storedMount, "inside.txt"));

    assertTrue(resolved.isPresent());
    assertEquals(file.toRealPath(), resolved.get());
  }

  @Test
  public void resolve_whenFileUnderSecondStoredMount_activePathReturned() throws IOException {
    Path subFolder = Files.createDirectory(otherActiveMount.resolve("sub"));
    Path file = Files.createFile(subFolder.resolve("inside.txt"));

    Optional<Path> resolved = newResolver().resolve(fileUrl(otherStoredMount, "sub/inside.txt"));

    assertTrue(resolved.isPresent());
    assertEquals(file.toRealPath(), resolved.get());
  }

  @Test
  public void resolve_whenNestedStoredMounts_mostSpecificUsed() throws IOException {
    Path nestedActive = Files.createDirectory(tempDir.toRealPath().resolve("nested-active"));
    Path file = Files.createFile(nestedActive.resolve("inside.txt"));
    ExternalResourceFileResolver resolver = newResolver(Map.of(
      storedMount.toString(), activeMount.toString(),
      storedMount.resolve("nested").toString(), nestedActive.toString()));

    Optional<Path> resolved = resolver.resolve(fileUrl(storedMount, "nested/inside.txt"));

    assertTrue(resolved.isPresent());
    assertEquals(file.toRealPath(), resolved.get());
  }

  @Test
  public void resolve_whenDotSegmentsStayInsideMount_normalizedPathReturned() throws IOException {
    Path file = Files.createFile(activeMount.resolve("inside.txt"));

    Optional<Path> resolved = newResolver().resolve(fileUrl(storedMount, "sub/../inside.txt"));

    assertTrue(resolved.isPresent());
    assertEquals(file.toRealPath(), resolved.get());
  }

  @Test
  public void resolve_whenMissingFileUnderStoredMount_normalizedActivePathReturned() {
    Optional<Path> resolved = newResolver().resolve(fileUrl(storedMount, "missing.txt"));

    assertTrue(resolved.isPresent());
    assertEquals(activeMount.resolve("missing.txt"), resolved.get());
  }

  @Test
  public void resolve_whenNotUnderStoredMount_empty() throws IOException {
    Path outsideFile = Files.createFile(notMounted.resolve("outside.txt"));
    ExternalResourceFileResolver resolver = newResolver();

    assertTrue(resolver.resolve(outsideFile.toUri().toString()).isEmpty());
    // same name prefix but a different folder
    assertTrue(resolver.resolve(fileUrl(storedMount.resolveSibling("abc-other"), "a.txt")).isEmpty());
  }

  @Test
  public void resolve_whenPathTraversalOutOfMount_throws() {
    ExternalResourceFileResolver resolver = newResolver();

    // e.g. file:///gpsc5/abc/../../etc/hosts
    assertThrows(SecurityException.class,
      () -> resolver.resolve(fileUrl(storedMount, "../../etc/hosts")));
    // into another mounted folder
    assertThrows(SecurityException.class,
      () -> resolver.resolve(fileUrl(storedMount, "../other/inside.txt")));
    // the mount point itself can't be escaped by its parent
    assertThrows(SecurityException.class, () -> resolver.resolve(fileUrl(storedMount, "..")));
  }

  @Test
  public void resolve_whenNonFileURL_empty() {
    assertTrue(newResolver().resolve("https://example.com/report.pdf").isEmpty());
  }

  @Test
  public void resolve_whenSymlinkEscapesMount_throws() throws IOException {
    Path outsideFile = Files.createFile(notMounted.resolve("symlink-target.txt"));
    try {
      Files.createSymbolicLink(activeMount.resolve("escaping-link.txt"), outsideFile);
    } catch (UnsupportedOperationException | IOException e) {
      Assumptions.assumeTrue(false, "Symbolic links are not supported: " + e.getMessage());
    }
    ExternalResourceFileResolver resolver = newResolver();
    assertThrows(SecurityException.class,
      () -> resolver.resolve(fileUrl(storedMount, "escaping-link.txt")));
  }

  @Test
  public void newResolver_whenRelativeMountPoint_throws() {
    assertThrows(IllegalArgumentException.class,
      () -> newResolver(Map.of("gpsc5abc", activeMount.toString())));
    assertThrows(IllegalArgumentException.class,
      () -> newResolver(Map.of(storedMount.toString(), "mnt/gpsc")));
  }
}
