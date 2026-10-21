package ca.gc.aafc.objectstore.api.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests related to {@link ExternalResourceFileResolver}
 */
public class ExternalResourceFileResolverTest {

  @TempDir
  Path tempDir;

  private ExternalResourceFileResolver newResolver() throws IOException {
    return new ExternalResourceFileResolver(tempDir.toRealPath().toString());
  }

  @Test
  public void resolve_whenNoBasePathConfigured_empty() throws IOException {
    Path file = Files.createFile(tempDir.resolve("no-base-path.txt"));
    ExternalResourceFileResolver resolver = new ExternalResourceFileResolver(" ");
    assertTrue(resolver.resolve(file.toUri().toString()).isEmpty());
  }

  @Test
  public void resolve_whenFileInsideBasePath_pathReturned() throws IOException {
    Path file = Files.createFile(tempDir.resolve("inside.txt"));

    Optional<Path> resolved = newResolver().resolve(file.toUri().toString());

    assertTrue(resolved.isPresent());
    assertEquals(file.toRealPath(), resolved.get());
  }

  @Test
  public void resolve_whenMissingFileInsideBasePath_normalizedPathReturned() throws IOException {
    Path missingFile = tempDir.resolve("missing.txt");

    Optional<Path> resolved = newResolver().resolve(missingFile.toUri().toString());

    assertTrue(resolved.isPresent());
    assertEquals(missingFile.toAbsolutePath().normalize(), resolved.get());
  }

  @Test
  public void resolve_whenFileOutsideBasePath_empty() throws IOException {
    Path outsideFile = Files.createTempFile("outside-base-path", ".txt");
    try {
      assertTrue(newResolver().resolve(outsideFile.toUri().toString()).isEmpty());
    } finally {
      Files.deleteIfExists(outsideFile);
    }
  }

  @Test
  public void resolve_whenPathTraversalOutOfBasePath_empty() throws IOException {
    Path outsideFile = Files.createTempFile("outside-path-traversal", ".txt");
    try {
      String traversingUrl = tempDir.toUri().resolve("../" + outsideFile.getFileName()).toString();
      assertTrue(newResolver().resolve(traversingUrl).isEmpty());
    } finally {
      Files.deleteIfExists(outsideFile);
    }
  }

  @Test
  public void resolve_whenNonFileURL_empty() throws IOException {
    assertTrue(newResolver().resolve("https://example.com/report.pdf").isEmpty());
  }

  @Test
  public void resolve_whenSymlinkEscapesBasePath_empty() throws IOException {
    Path outsideFile = Files.createTempFile("outside-symlink-target", ".txt");
    Path escapingLink = tempDir.resolve("escaping-link.txt");
    try {
      try {
        Files.createSymbolicLink(escapingLink, outsideFile);
      } catch (UnsupportedOperationException | IOException e) {
        Assumptions.assumeTrue(false, "Symbolic links are not supported: " + e.getMessage());
      }
      assertTrue(newResolver().resolve(escapingLink.toUri().toString()).isEmpty());
    } finally {
      Files.deleteIfExists(escapingLink);
      Files.deleteIfExists(outsideFile);
    }
  }
}
