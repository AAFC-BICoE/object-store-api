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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@ContextConfiguration(initializers = MinioTestContainerInitializer.class)
@SpringBootTest(properties = {"dev-user.enabled=true", "dina.fileStorage.implementation=MINIO"})
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
      BUCKET,
      fileName,
      false,
      MediaType.TEXT_PLAIN_VALUE,
      new ByteArrayInputStream(firstFile)
    );
    Assertions.assertArrayEquals(firstFile, returnBytesForFile(fileName));

    byte[] expected = "dina".getBytes();
    fileService.storeFile(
      BUCKET,
      fileName,
      false,
      MediaType.TEXT_PLAIN_VALUE,
      new ByteArrayInputStream(expected)
    );

    Assertions.assertArrayEquals(expected, returnBytesForFile(fileName));
  }

  @SneakyThrows
  @Test
  void storeFile_getFile_FilesStoredAndGotten() {
    byte[] bytes = "dina".getBytes();
    String fileName = "name";

    fileService.storeFile(
      BUCKET,
      fileName,
      false,
      MediaType.TEXT_PLAIN_VALUE,
      new ByteArrayInputStream(bytes)
    );

    Assertions.assertArrayEquals(bytes, returnBytesForFile(fileName));
  }

  @SneakyThrows
  @Test
  void getFile_WhenFileDoesNotExist_OptionalEmptyReturned() {
    Assertions.assertFalse(fileService.retrieveFile(BUCKET,"fileName", false).isPresent());
  }

  @SneakyThrows
  @Test
  void getFile_WhenBucketDoesNotExist_OptionalEmptyReturned() {
    byte[] bytes = "dina".getBytes();
    String fileName = "name";

    fileService.storeFile(
      BUCKET,
      fileName,
      false,
      MediaType.TEXT_PLAIN_VALUE,
      new ByteArrayInputStream(bytes)
    );

    Assertions.assertFalse(fileService.retrieveFile("fake", fileName,false).isPresent());
  }

  @SneakyThrows
  @Test
  void getFileInfo_ReturnsFileInfo() {
    byte[] bytes = "dina".getBytes();
    String fileName = "name";

    fileService.storeFile(
      BUCKET,
      fileName,
      false,
      MediaType.TEXT_PLAIN_VALUE,
      new ByteArrayInputStream(bytes)
    );

    Assertions.assertTrue(fileService.getFileInfo(BUCKET, fileName,false).isPresent());
  }

  @SneakyThrows
  @Test
  void getFileInfo_WhenNoFile_OptionalEmptyReturned() {
    Assertions.assertFalse(fileService.getFileInfo(BUCKET, "nosuchfile",false).isPresent());
  }

  @SneakyThrows
  @Test
  void removeFile_FileRemoved() {
    byte[] bytes = "dina".getBytes();
    String fileName = "name";

    fileService.storeFile(
      BUCKET,
      fileName,
      false,
      MediaType.TEXT_PLAIN_VALUE,
      new ByteArrayInputStream(bytes)
    );

    Assertions.assertTrue(fileService.retrieveFile(BUCKET, fileName,false).isPresent());
    fileService.deleteFile(BUCKET, fileName, false);
    Assertions.assertFalse(fileService.retrieveFile(BUCKET, fileName, false).isPresent());
  }

  @Test
  void bucketExists() {
    Assertions.assertTrue(fileService.bucketExists(BUCKET));
    Assertions.assertFalse(fileService.bucketExists("fake"));
  }

  private byte[] returnBytesForFile(String fileName) throws IOException {
    return IOUtils.toByteArray(fileService.retrieveFile(BUCKET, fileName, false)
      .orElseThrow(() -> {
        Assertions.fail("The file was not persisted");
        return null;
      }));
  }
}