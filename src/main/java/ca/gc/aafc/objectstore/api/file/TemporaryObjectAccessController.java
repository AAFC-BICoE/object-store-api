package ca.gc.aafc.objectstore.api.file;

import org.apache.commons.io.FilenameUtils;
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
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.PreDestroy;
import lombok.extern.log4j.Log4j2;

import ca.gc.aafc.dina.file.FileCleaner;

/**
 * Allows a single, temporary access to a specific file that is also temporary.
 * Files are stored in specific folder that should only be used for temporary object access.
 * Once used or expired, the file will be deleted.
 */
@RestController
@RequestMapping("/api/v1")
@Log4j2
public class TemporaryObjectAccessController {

  private static final Set<String> SUPPORTED_FILE_EXT = Set.of("txt", "zip", "ftlh");

  private static final Path WORKING_FOLDER = assignWorkingDir();
  private static final long MAX_AGE_MINUTES = 60;
  private static final TemporalAmount MAX_AGE = Duration.ofMinutes(MAX_AGE_MINUTES);

  private static final ConcurrentHashMap<String, TemporaryObjectAccess> ACCESS_MAP
    = new ConcurrentHashMap<>();

  private final FileCleaner fileCleaner;

  private static Path assignWorkingDir() {
    try {
      return Files.createTempDirectory("dina_obj_export");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public TemporaryObjectAccessController() {
    fileCleaner = FileCleaner.newInstance(WORKING_FOLDER,
      FileCleaner.buildMaxAgePredicate(ChronoUnit.SECONDS, MAX_AGE_MINUTES * 60)
        .and(buildFileExtensionPredicate()));
  }

  private static Predicate<Path> buildFileExtensionPredicate() {

    Optional<String> firstExt = SUPPORTED_FILE_EXT.stream().findFirst();

    if (firstExt.isEmpty()) {
      throw new IllegalStateException("Requires at least 1 extension");
    }

    // create a predicate for the first file extension
    Predicate<Path> fileExtensionPredicate =
      FileCleaner.buildFileExtensionPredicate(firstExt.get());
    Set<String> filteredFileExt =
      SUPPORTED_FILE_EXT.stream().filter(s -> !s.equals(firstExt.get())).collect(
        Collectors.toSet());

    // if there is more than 1, combine with an OR
    for (String ext : filteredFileExt) {
      fileExtensionPredicate =
        fileExtensionPredicate.or(FileCleaner.buildFileExtensionPredicate(ext));
    }
    return fileExtensionPredicate;
  }

  /**
   * Generate a path to store a temporary file that should be available for
   * temporary object access.
   * @param filename
   * @return
   */
  public Path generatePath(String filename) {
    return WORKING_FOLDER.resolve(filename);
  }

  /**
   * Register the file so it becomes available for download (a single time) using the returned key.
   * @param filename
   * @return the single-use TemporaryObjectAccess key that can be used to download the file
   */
  public String registerObject(String filename) {

    // run the clean method on new requests to avoid a scheduled method (at least for now)
    cleanExpiredFile();

    // make sure the object exists
    if (!WORKING_FOLDER.resolve(filename).toFile().exists()) {
      throw new IllegalArgumentException("the file must exist");
    }

    if (!SUPPORTED_FILE_EXT.contains(FilenameUtils.getExtension(filename))) {
      throw new IllegalArgumentException("unsupported file extension");
    }

    String key = RandomStringUtils.randomAlphanumeric(128);
    TemporaryObjectAccess toa = new TemporaryObjectAccess(filename, Instant.now());
    ACCESS_MAP.put(key, toa);
    return key;
  }

  /**
   * Triggers a download of a file.
   *
   * @param key the tao key
   * @return a response entity
   */
  @GetMapping("/toa/{key}")
  public ResponseEntity<InputStreamResource> downloadObject(
    @PathVariable String key
  ) throws IOException {
    TemporaryObjectAccess toa = ACCESS_MAP.remove(key);

    if (toa == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // make sure the toa is not expired
    if (Instant.now().isAfter(toa.createdOn.plus(MAX_AGE))) {
      log.warn("toa expired");
      cleanExpiredFile();
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    File f = WORKING_FOLDER.resolve(toa.filename()).toFile();
    return new ResponseEntity<>(
      new InputStreamResource(Files.newInputStream(WORKING_FOLDER.resolve(toa.filename()))),
      buildHttpHeaders(toa.filename(), MediaType.OCTET_STREAM.toString(), f.length()),
      HttpStatus.OK);
  }

  @PreDestroy
  public void cleanAllFiles() {
    // clean all files since we will lose the keys anyway
    try {
      FileCleaner.newInstance(WORKING_FOLDER, buildFileExtensionPredicate()).clean();
    } catch (IOException e) {
      log.warn("Unable clear temporary files on exit");
    }
  }

  private void cleanExpiredFile() {
    try {
      fileCleaner.clean();
    } catch (IOException e) {
      log.warn("Unable clear expired files in TemporaryObjectAccessController");
    }
  }

  record TemporaryObjectAccess(String filename, Instant createdOn) {
  }
}
