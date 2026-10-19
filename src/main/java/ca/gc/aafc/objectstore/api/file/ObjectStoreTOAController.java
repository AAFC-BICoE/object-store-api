package ca.gc.aafc.objectstore.api.file;

import java.io.IOException;
import java.util.Set;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.gc.aafc.dina.file.TemporaryObjectAccessController;
import lombok.extern.log4j.Log4j2;

/**
 * Allows a single, temporary access to a specific file that is also temporary.
 * Files are stored in specific folder that should only be used for temporary object access (TOA).
 * Once used or expired, the file will be deleted.
 */
@RestController
@RequestMapping("/api/v1")
@Log4j2
public class ObjectStoreTOAController extends TemporaryObjectAccessController {

  private static final Set<String> SUPPORTED_FILE_EXT = Set.of("txt", "zip", "ftlh");

  public ObjectStoreTOAController() {
    super(SUPPORTED_FILE_EXT);
  }

  /**
   * Triggers a download of a file.
   *
   * @param key the tao key
   * @return a response entity
   */
  @GetMapping("/toa/{key}")
  public ResponseEntity<InputStreamResource> handleDownload(@PathVariable String key) throws IOException {
    return downloadObject(key);
  }
}
