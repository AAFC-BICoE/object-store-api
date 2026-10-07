package ca.gc.aafc.objectstore.api;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

import ca.gc.aafc.objectstore.api.service.MigrationHintsService;
import ca.gc.aafc.objectstore.api.storage.XlMetaFileDetector;

import java.io.IOException;
import java.nio.file.Path;

import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Value;

/**
 * Checks run once all singletons are instantiated, which is before the embedded web server starts
 * accepting requests. A failing check stops the application before it can be considered ready
 * (e.g. by a Kubernetes/OpenShift readiness probe checking the port).
 */
@Log4j2
@Component
public class StartupChecks implements SmartInitializingSingleton {

  private static final String MARKER_KEY = "no_xlmeta_marker";

  @Value("${dina.fileStorage.implementation:}")
  private String storageImpl;

  @Value("${dina.fileStorage.root:}")
  private String storageRoot;

  private final MigrationHintsService migrationHintsService;

  public StartupChecks(MigrationHintsService migrationHintsService) {
    this.migrationHintsService = migrationHintsService;
  }

  @Override
  public void afterSingletonsInstantiated() {
    checkFileSystemConfig();
    checkLegacyMinioFileSystem();
  }

  /**
   * Checks legacy file system storage implementation.
   * <p>
   * If the storage implementation is configured as "FS" and a storage root is provided,
   * it scans the file system tree to see if the previous data in that root was erasure coded (MinIO xl.meta files):
   * <ul>
   * <li>If an xl.meta file is found, an {@link IllegalStateException} is thrown so the application doesn't start.</li>
   * <li>If some entries could not be read, the check is considered incomplete and will run again on the next
   * startup.</li>
   * <li>Otherwise, a marker is set so the check is only run once.</li>
   * </ul>
   */
  private void checkLegacyMinioFileSystem() {
    if (migrationHintsService.hasHint(MARKER_KEY)) {
      log.info("FS startup check already completed, skipping...");
    } else {
      try {
        if ("FS".equalsIgnoreCase(storageImpl) && StringUtils.isNotBlank(storageRoot)) {
          log.info("FS startup check: scanning {} for legacy xl.meta files", storageRoot);
          Path rootPath = Path.of(storageRoot);
          XlMetaFileDetector detector = new XlMetaFileDetector();
          java.nio.file.Files.walkFileTree(rootPath, detector);
          if (detector.isFoundXlMeta()) {
            // the app should not start if xl.meta is found
            throw new IllegalStateException("FS startup check failed: Legacy xl.meta found.");
          }
          if (detector.isScanComplete()) {
            migrationHintsService.saveHint(MARKER_KEY);
            log.info("FS startup check complete, marker set.");
          } else {
            log.warn("FS startup check incomplete: some paths could not be read, marker not set.");
          }
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
