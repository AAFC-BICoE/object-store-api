package ca.gc.aafc.objectstore.api.minio;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;

class MinioFileServiceTest extends BaseIntegrationTest {

  public static final String BUCKET = "bucket";

  @Inject
  private MinioFileService fileService;

  @Inject
  private MinioClient client;

  @SneakyThrows
  @BeforeEach
  void setUp() {
    if (!client.bucketExists(BucketExistsArgs.builder().bucket(BUCKET).build())) {
      client.makeBucket(MakeBucketArgs.builder().bucket(BUCKET).build());
    }
  }

  @SneakyThrows
  @Test
  void storeFile_whenFileExists_FileOverWritten() {
    String fileName = "name";
    byte[] firstFile = "firstFile".getBytes();
    fileService.storeFile(
      fileName,
      new ByteArrayInputStream(firstFile),
      MediaType.TEXT_PLAIN_VALUE,
      BUCKET,
      false);
    Assertions.assertArrayEquals(firstFile, returnBytesForFile(fileName));

    byte[] expected = "dina".getBytes();
    fileService.storeFile(
      fileName,
      new ByteArrayInputStream(expected),
      MediaType.TEXT_PLAIN_VALUE,
      BUCKET,
      false);

    Assertions.assertArrayEquals(expected, returnBytesForFile(fileName));
  }

  @SneakyThrows
  @Test
  void storeFile_getFile_FilesStoredAndGotten() {
    byte[] bytes = "dina".getBytes();
    String fileName = "name";

    fileService.storeFile(
      fileName,
      new ByteArrayInputStream(bytes),
      MediaType.TEXT_PLAIN_VALUE,
      BUCKET,
      false);

    Assertions.assertArrayEquals(bytes, returnBytesForFile(fileName));
  }

  @SneakyThrows
  @Test
  void getFile_WhenFileDoesNotExist_OptionalEmptyReturned() {
    Assertions.assertFalse(fileService.getFile("fileName", BUCKET, false).isPresent());
  }

  @SneakyThrows
  @Test
  void getFile_WhenBucketDoesNotExist_OptionalEmptyReturned() {
    byte[] bytes = "dina".getBytes();
    String fileName = "name";

    fileService.storeFile(
      fileName,
      new ByteArrayInputStream(bytes),
      MediaType.TEXT_PLAIN_VALUE,
      BUCKET,
      false);

    Assertions.assertFalse(fileService.getFile(fileName, "fake", false).isPresent());
  }

  @SneakyThrows
  @Test
  void getFileInfo_ReturnsFileInfo() {
    byte[] bytes = "dina".getBytes();
    String fileName = "name";

    fileService.storeFile(
      fileName,
      new ByteArrayInputStream(bytes),
      MediaType.TEXT_PLAIN_VALUE,
      BUCKET,
      false);

    Assertions.assertTrue(fileService.getFileInfo(fileName, BUCKET, false).isPresent());
  }

  @SneakyThrows
  @Test
  void getFileInfo_WhenNoFile_OptionalEmptyReturned() {
    Assertions.assertFalse(fileService.getFileInfo("nosuchfile", BUCKET, false).isPresent());
  }

  @SneakyThrows
  @Test
  void removeFile_FileRemoved() {
    byte[] bytes = "dina".getBytes();
    String fileName = "name";

    fileService.storeFile(
      fileName,
      new ByteArrayInputStream(bytes),
      MediaType.TEXT_PLAIN_VALUE,
      BUCKET,
      false);

    Assertions.assertTrue(fileService.getFile(fileName, BUCKET, false).isPresent());
    fileService.removeFile(BUCKET, fileName);
    Assertions.assertFalse(fileService.getFile(fileName, BUCKET, false).isPresent());
  }

  @Test
  void bucketExists() {
    Assertions.assertTrue(fileService.bucketExists(BUCKET));
    Assertions.assertFalse(fileService.bucketExists("fake"));
  }

  private byte[] returnBytesForFile(String fileName) throws IOException {
    return IOUtils.toByteArray(fileService.getFile(fileName, BUCKET, false)
      .orElseThrow(() -> {
        Assertions.fail("The file was not persisted");
        return null;
      }));
  }
}