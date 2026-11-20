package ca.gc.aafc.objectstore.api.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ca.gc.aafc.objectstore.api.config.ObjectExportOption;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.image.ImageOperationService;
import ca.gc.aafc.objectstore.api.storage.FileStorage;

/**
 * Tests related to {@link ObjectExportGenerator} (no Spring context, no database).
 */
public class ObjectExportGeneratorTest {

  @TempDir
  Path tempDir;

  private final FileStorage fileStorage = mock(FileStorage.class);
  private final ObjectExportGenerator generator =
    new ObjectExportGenerator(fileStorage, mock(ImageOperationService.class));

  private static ObjectStoreMetadata externalMetadata(String filename) {
    ObjectStoreMetadata metadata = new ObjectStoreMetadata();
    metadata.setUuid(UUID.randomUUID());
    metadata.setFilename(filename);
    metadata.setResourceExternalURL("file:///mnt/nfs/whatever");
    return metadata;
  }

  private static ObjectStoreMetadata storedMetadata(UUID fileIdentifier, String filename) {
    ObjectStoreMetadata metadata = new ObjectStoreMetadata();
    metadata.setUuid(UUID.randomUUID());
    metadata.setBucket("bucket");
    metadata.setFileIdentifier(fileIdentifier);
    metadata.setFileExtension(".txt");
    metadata.setOriginalFilename(filename);
    return metadata;
  }

  private Path writeFile(String name, String content) throws IOException {
    return Files.writeString(tempDir.resolve(name), content);
  }

  private Map<String, String> exportAndRead(List<ObjectExportItem> items)
      throws IOException, ExecutionException, InterruptedException {
    Path zip = tempDir.resolve(UUID.randomUUID() + ".zip");
    generator.export(UUID.randomUUID(), items, zip, ObjectExportOption.builder().build()).get();

    // LinkedHashMap-like ordering is not required, only names and content
    Map<String, String> entries = new java.util.TreeMap<>();
    try (ZipFile zipFile = ZipFile.builder().setPath(zip).get()) {
      for (ZipArchiveEntry entry : new ArrayList<>(java.util.Collections.list(zipFile.getEntries()))) {
        entries.put(entry.getName(),
          new String(zipFile.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8));
      }
    }
    return entries;
  }

  @Test
  public void export_externalResource_contentReadFromLocalPathNotFileStorage()
      throws Exception {
    Path file = writeFile("source-on-disk.txt", "external content");
    var item = new ObjectExportItem.ExternalResource(externalMetadata("my:report.txt"), null, null, file);

    Map<String, String> entries = exportAndRead(List.of(item));

    assertEquals(Map.of("my_report.txt", "external content"), entries);
  }

  @Test
  public void export_externalResourceWithAliasAndFolder_aliasKeepsRealExtension() throws Exception {
    Path file = writeFile("source.csv", "a,b");
    var item = new ObjectExportItem.ExternalResource(externalMetadata("ignored.txt"), "my-alias", "folderA", file);

    Map<String, String> entries = exportAndRead(List.of(item));

    assertEquals(1, entries.size());
    assertTrue(entries.containsKey("folderA/my-alias.csv"), entries.keySet().toString());
  }

  @Test
  public void export_externalResourceWithoutMetadataFilename_fallsBackToResolvedFilename() throws Exception {
    Path file = writeFile("on-disk-name.txt", "x");
    var item = new ObjectExportItem.ExternalResource(externalMetadata(null), null, null, file);

    assertEquals(List.of("on-disk-name.txt"), List.copyOf(exportAndRead(List.of(item)).keySet()));
  }

  @Test
  public void export_duplicatedExternalFilenames_numbered() throws Exception {
    Path f1 = writeFile("one.txt", "1");
    Path f2 = writeFile("two.txt", "2");
    var i1 = new ObjectExportItem.ExternalResource(externalMetadata("same.txt"), null, null, f1);
    var i2 = new ObjectExportItem.ExternalResource(externalMetadata("same.txt"), null, null, f2);

    Map<String, String> entries = exportAndRead(List.of(i1, i2));

    assertEquals(Map.of("same.txt", "1", "same(1).txt", "2"), entries);
  }

  @Test
  public void export_storedAndExternalMixed_eachReadFromItsSource() throws Exception {
    UUID fileId = UUID.randomUUID();
    when(fileStorage.retrieveFile(anyString(), anyString(), anyBoolean())).thenReturn(
      Optional.of(new ByteArrayInputStream("stored content".getBytes(StandardCharsets.UTF_8))));

    var stored = new ObjectExportItem.StoredFile(storedMetadata(fileId, "stored.txt"), null, null);
    var external = new ObjectExportItem.ExternalResource(externalMetadata("ext.txt"), null, "ext",
      writeFile("ext-src.txt", "external content"));

    Map<String, String> entries = exportAndRead(List.of(stored, external));

    assertEquals(Map.of("stored.txt", "stored content", "ext/ext.txt", "external content"), entries);
  }

  private Throwable exportFailure(Path externalPath) {
    var item = new ObjectExportItem.ExternalResource(externalMetadata("x.txt"), null, null, externalPath);
    var future = generator.export(UUID.randomUUID(), List.of(item),
      tempDir.resolve(UUID.randomUUID() + ".zip"), ObjectExportOption.builder().build());
    assertTrue(future.isCompletedExceptionally());
    return assertThrows(ExecutionException.class, future::get).getCause();
  }

  @Test
  public void export_externalResourceFileMissing_failedFuture() {
    assertInstanceOf(IllegalStateException.class, exportFailure(tempDir.resolve("does-not-exist.txt")));
  }

  @Test
  public void export_externalResourceIsDirectory_failedFuture() throws IOException {
    Path dir = Files.createDirectory(tempDir.resolve("a-directory"));
    assertInstanceOf(IllegalStateException.class, exportFailure(dir));
  }

  @Test
  public void export_externalResourceIsSymlink_failedFuture() throws IOException {
    Path target = writeFile("target.txt", "content");
    Path link = Files.createSymbolicLink(tempDir.resolve("link.txt"), target);
    assertInstanceOf(IllegalStateException.class, exportFailure(link));
  }

  @Test
  public void export_externalResourceNotOnLocalFileSystem_failedFuture() throws IOException {
    Path zip = tempDir.resolve("container.zip");
    try (FileSystem zipFs = FileSystems.newFileSystem(URI.create("jar:" + zip.toUri()),
        Map.of("create", "true"))) {
      Path inZip = Files.writeString(zipFs.getPath("/inside.txt"), "content");
      assertInstanceOf(IllegalStateException.class, exportFailure(inZip));
    }
  }

  @Test
  public void getInternalFilename_noFileIdentifier_null() {
    assertEquals(null, externalMetadata("x.txt").getInternalFilename());
  }
}
