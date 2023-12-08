package ca.gc.aafc.objectstore.api.file;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.tika.mime.MediaType;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static ca.gc.aafc.objectstore.api.file.FileController.buildHttpHeaders;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping("/api/v1")
@Log4j2
public class TemporaryObjectAccessController {

  private static final Path WORKING_FOLDER = assignWorkingDir();

  private static final ConcurrentHashMap<String, TemporaryAccessObject> ACCESS_MAP
    = new ConcurrentHashMap<>();

  private static Path assignWorkingDir() {
    try {
      return Files.createTempDirectory("dina_obj_export");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Path generatePath(String filename) {
    return WORKING_FOLDER.resolve(filename);
  }

  public String registerObject(String filename) {

    // make sure the object exists
    if(!WORKING_FOLDER.resolve(filename).toFile().exists()) {
      throw new IllegalArgumentException("the file must exist");
    }

    String key = RandomStringUtils.randomAlphanumeric(128);
    ACCESS_MAP.put(key, new TemporaryAccessObject(filename));
    return key;
  }

  /**
   * Triggers a download of a file. Note that the file requires a metadata entry in the database to be
   * available for download.
   *
   * @param key the bucket
   * @return a response entity
   */
  @GetMapping("/tao/{key}")
  public ResponseEntity<InputStreamResource> downloadObject(
    @PathVariable String key
  ) throws IOException {
    TemporaryAccessObject tao = ACCESS_MAP.remove(key);
    if(tao == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    File f = WORKING_FOLDER.resolve(tao.filename()).toFile();

    return new ResponseEntity<>(
      new InputStreamResource(Files.newInputStream(WORKING_FOLDER.resolve(tao.filename()))),
      buildHttpHeaders(tao.filename(), MediaType.OCTET_STREAM.toString(), f.length()),
      HttpStatus.OK);
  }

  record TemporaryAccessObject(String filename){}
}
