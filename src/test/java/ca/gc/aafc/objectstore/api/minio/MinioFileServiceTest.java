package ca.gc.aafc.objectstore.api.minio;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;

class MinioFileServiceTest extends BaseIntegrationTest {

  private static final MinioTestContainer minioTestContainer = MinioTestContainer.getInstance();
  public static final String BUCKET = "bucket";

  @Inject
  private MinioFileService fileService;

  @Inject
  private MinioClient client;

  @BeforeAll
  static void beforeAll() {
    minioTestContainer.start();
  }

  @SneakyThrows
  @BeforeEach
  void setUp() {
    client.makeBucket(MakeBucketArgs.builder().bucket(BUCKET).build());
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

    byte[] resultBytes = IOUtils.toByteArray(fileService.getFile(fileName, BUCKET, false)
      .orElseThrow(() -> {
        Assertions.fail("The file was not persisted");
        return null;
      }));
    Assertions.assertArrayEquals(bytes, resultBytes);
  }

}