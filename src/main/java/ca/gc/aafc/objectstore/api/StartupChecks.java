package ca.gc.aafc.objectstore.api;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import ca.gc.aafc.objectstore.api.service.MigrationHintsService;
import ca.gc.aafc.objectstore.api.storage.XlMetaFileDetector;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.io.IOException;
import java.nio.file.Path;

import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Value;

@Log4j2
@Component
public class StartupChecks {

  private static final String MARKER_KEY = "no_xlmeta_marker";

  @Value("${dina.fileStorage.implementation:}")
  private String storageImpl;

  @Value("${dina.fileStorage.root:}")
  private String storageRoot;

  private final MigrationHintsService migrationHintsService;

  public StartupChecks(MigrationHintsService migrationHintsService) {
    this.migrationHintsService = migrationHintsService;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void onAppReady() {
    checkFileSystemConfig();
    checkLegacyMinioFileSystem();
  }

  /**
   * Checks legacy file system storage implementation.
   * <p>
   * This method is triggered when the application is ready and performs the
   * following checks:
   * <ul>
   * <li>If the storage implementation is configured as "FS" and a storage root is provided,
   * it scans the file system tree to see if the previous data in that root was erasure coded.
   * Once the check is successfully completed, a marker is set so the check is only run once.</li>
   * </ul>
   * <p>
   */
  private void checkLegacyMinioFileSystem() {
    if (migrationHintsService.hasHint(MARKER_KEY)) {
      log.info("FS startup check already completed, skipping...");
    } else {
      try {
        if ("FS".equalsIgnoreCase(storageImpl) && StringUtils.isBlank(storageRoot)) {
          Path rootPath = Path.of(storageRoot);
          XlMetaFileDetector detector = new XlMetaFileDetector();
          java.nio.file.Files.walkFileTree(rootPath, detector);
          if (!detector.isFoundXlMeta()) {
            migrationHintsService.saveHint(MARKER_KEY);
            log.info("FS startup check complete, marker set.");
          }
          // the app should not start if xl.meta is found
          throw new IllegalStateException("FS startup check failed: Legacy xl.meta found.");
        }
      } catch (IOException e) {
        log.error("FS startup check failed: {}", e.getMessage());
      }
    }
  }

  private void checkFileSystemConfig() {
    if ("FS".equalsIgnoreCase(storageImpl)) {
      if (StringUtils.isBlank(storageRoot)) {
        throw new IllegalStateException("FS startup check failed: fileStorage.root must be defined in FS Mode");
      }
    }
  }
}
