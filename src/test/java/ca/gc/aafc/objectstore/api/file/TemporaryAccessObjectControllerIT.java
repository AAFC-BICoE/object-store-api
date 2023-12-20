package ca.gc.aafc.objectstore.api.file;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TemporaryAccessObjectControllerIT extends BaseIntegrationTest {

  @Inject
  private TemporaryObjectAccessController tempObjectAccessController;

  @Test
  public void toa_onObjectRegistered_objectCanBeDownloadedOnce() throws IOException {

    final String testText = "this is a test";
    final String testFilename = UUID.randomUUID() + ".txt";

    Path p = tempObjectAccessController.generatePath(testFilename);
    Files.writeString(p, testText);

    String key = tempObjectAccessController.registerObject(testFilename);
    ResponseEntity<InputStreamResource> response = tempObjectAccessController.downloadObject(key);

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    InputStream is = response.getBody().getInputStream();
    IOUtils.copy(is, os);

    is.close();

    String str = os.toString(StandardCharsets.UTF_8);
    assertEquals(testText, str);

    // try again
    response = tempObjectAccessController.downloadObject(key);
    assertEquals(404, response.getStatusCode().value());

  }
}
