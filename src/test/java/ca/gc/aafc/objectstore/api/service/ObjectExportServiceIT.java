package ca.gc.aafc.objectstore.api.service;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.tika.mime.MimeTypeException;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.async.AsyncConsumer;
import ca.gc.aafc.objectstore.api.config.AsyncOverrideConfig;
import ca.gc.aafc.objectstore.api.config.ExportFunction;
import ca.gc.aafc.objectstore.api.config.ObjectExportOption;
import ca.gc.aafc.objectstore.api.dto.ObjectUploadDto;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.file.FileController;
import ca.gc.aafc.objectstore.api.file.TemporaryObjectAccessController;
import ca.gc.aafc.objectstore.api.minio.MinioFileService;
import ca.gc.aafc.objectstore.api.minio.MinioTestContainerInitializer;
import ca.gc.aafc.objectstore.api.testsupport.factories.MultipartFileFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.inject.Inject;

@ContextConfiguration(initializers = MinioTestContainerInitializer.class)
@Import(AsyncOverrideConfig.class)
public class ObjectExportServiceIT extends BaseIntegrationTest {

  private static final String TEST_UPLOAD_FILE_NAME = "drawing.png";
  private final static String TEST_BUCKET_NAME = "test";

  @Inject
  private ResourceLoader resourceLoader;

  @Inject
  private FileController fileController;

  @Inject
  private MinioFileService minioFileService;

  @Inject
  private ObjectExportService objectExportService;

  @Inject
  private AsyncConsumer<Future<ObjectExportService.ExportResult>> asyncConsumer;

  @Inject
  private TemporaryObjectAccessController toaController;

  @Test
  public void exportObjects_onExport_ZipContentValid()
    throws IOException, MimeTypeException, NoSuchAlgorithmException {

    // 1 - Upload file
    MockMultipartFile mockFile = MultipartFileFactory
      .createMockMultipartFile(resourceLoader, TEST_UPLOAD_FILE_NAME, MediaType.IMAGE_PNG_VALUE);

    ObjectUploadDto uploadResponse = fileController.handleFileUpload(mockFile, TEST_BUCKET_NAME);
    assertNotNull(uploadResponse);
    assertNotNull(uploadResponse.getFileIdentifier());

    // 2 - Created metadata for it
    ObjectStoreMetadata osm = ObjectStoreMetadataFactory
      .newObjectStoreMetadata()
      .bucket(TEST_BUCKET_NAME)
      .fileIdentifier(uploadResponse.getFileIdentifier())
      .build();
    objectStoreMetaDataService.create(osm);

    // 3 - Make sure a thumbnail is generated
    Optional<Derivative> thumbnail = derivativeService.findThumbnailDerivativeForMetadata(osm);
    assertTrue(thumbnail.isPresent());

    // 4 - request the file and its derivative
    objectExportService.export(ObjectExportService.ExportArgs.builder()
      .username("testuser")
      .fileIdentifiers(List.of(osm.getFileIdentifier(), thumbnail.get().getFileIdentifier()))
      .objectExportOption(ObjectExportOption.builder()
        .exportLayout(Map.of("thumb/", List.of(thumbnail.get().getFileIdentifier())))
        .aliases(Map.of(osm.getFileIdentifier(), "testFileAlias"))
        .build())
      .username("testname")
      .build());

    // 5 - Wait for completion
    ObjectExportService.ExportResult result;
    try {
      result = asyncConsumer.getAccepted().getFirst().get();
      asyncConsumer.clear();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }

    // 6 - Make sure we can get the export file using the toa key
    ResponseEntity<InputStreamResource> response = toaController.downloadObject(result.toaKey());
    assertEquals(200, response.getStatusCode().value());

    Set<String> filenamesInZip = new HashSet<>();
    try (ZipArchiveInputStream archive = new ZipArchiveInputStream(
      response.getBody().getInputStream())) {
      ZipArchiveEntry entry;
      while ((entry = archive.getNextZipEntry()) != null) {
        filenamesInZip.add(entry.getName());
      }
    } catch (IOException e) {
      fail();
    }

    assertEquals(2, filenamesInZip.size());
    assertTrue(filenamesInZip.contains("testFileAlias.png"));
    assertTrue(filenamesInZip.contains("thumb/testfile_thumbnail.jpg"));
  }

  @Test
  public void exportObjects_onExportWithFunction_ZipContentValid()
    throws IOException, MimeTypeException, NoSuchAlgorithmException {

    // 1 - Upload file
    MockMultipartFile mockFile = MultipartFileFactory
      .createMockMultipartFile(resourceLoader, "cc0_test_image.jpg", MediaType.IMAGE_JPEG_VALUE);

    ObjectUploadDto uploadResponse = fileController.handleFileUpload(mockFile, TEST_BUCKET_NAME);
    assertNotNull(uploadResponse);
    assertNotNull(uploadResponse.getFileIdentifier());

    // 2 - Created metadata for it
    ObjectStoreMetadata osm = ObjectStoreMetadataFactory
      .newObjectStoreMetadata()
      .bucket(TEST_BUCKET_NAME)
      .fileIdentifier(uploadResponse.getFileIdentifier())
      .build();
    objectStoreMetaDataService.create(osm);

    // 3 - Make sure a thumbnail is generated
    Optional<Derivative> thumbnail = derivativeService.findThumbnailDerivativeForMetadata(osm);
    assertTrue(thumbnail.isPresent());

    // 4 - request the file and its derivative
    objectExportService.export(ObjectExportService.ExportArgs.builder()
      .username("testuser")
      .fileIdentifiers(List.of(osm.getFileIdentifier()))
      .objectExportOption(ObjectExportOption.builder()
        .functions(
          List.of(ExportFunction.builder().functionName(ExportFunction.FunctionName.IMG_RESIZE)
            .params(List.of("0.5")).build()))
        .build())
      .username("testname")
      .build());

    // 5 - Wait for completion
    ObjectExportService.ExportResult result;
    try {
      result = asyncConsumer.getAccepted().getFirst().get();
      asyncConsumer.clear();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }

    // 6 - Make sure we can get the export file using the toa key
    ResponseEntity<InputStreamResource> response = toaController.downloadObject(result.toaKey());
    assertEquals(200, response.getStatusCode().value());

    Set<String> filenamesInZip = new HashSet<>();
    try (ZipArchiveInputStream archive = new ZipArchiveInputStream(
      response.getBody().getInputStream())) {
      ZipArchiveEntry entry;
      while ((entry = archive.getNextZipEntry()) != null) {
        filenamesInZip.add(entry.getName());
      }
    } catch (IOException e) {
      fail();
    }

    assertEquals(1, filenamesInZip.size());
    assertTrue(filenamesInZip.contains("testfile.jpg"));
  }

}
