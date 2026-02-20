package ca.gc.aafc.objectstore.api.rest;


import java.nio.file.Path;

import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import ca.gc.aafc.dina.testsupport.BaseRestAssuredTest;

/**
 * object-store specific {@link BaseRestAssuredTest}
 */
public class ObjectStoreBaseRestAssuredTest extends BaseRestAssuredTest {

  // let junit create a temp dir for us
  @TempDir
  static Path rootTempDir;

  // replace the root with that tmp dir to make sure it exists
  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("dina.fileStorage.root", () -> rootTempDir.toAbsolutePath().toString());
  }
  protected ObjectStoreBaseRestAssuredTest(String basePath) {
    super(basePath);
  }
}
