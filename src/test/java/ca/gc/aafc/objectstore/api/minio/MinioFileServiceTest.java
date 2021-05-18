package ca.gc.aafc.objectstore.api.minio;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MinioFileServiceTest extends BaseIntegrationTest {

  private static final MinioTestContainer minioTestContainer = MinioTestContainer.getInstance();

  @BeforeAll
  static void beforeAll() {
    minioTestContainer.start();
  }

  @Test
  void name() {
    System.out.println(minioTestContainer.getHostAddress());
  }

}