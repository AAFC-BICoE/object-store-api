package ca.gc.aafc.objectstore.api;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import ca.gc.aafc.objectstore.api.service.MigrationHintsService;
import ca.gc.aafc.objectstore.api.storage.XlMetaFileDetector;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.nio.file.Path;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;

@Log4j2
@Component
public class StartupChecks {

    @Value("${dina.fileStorage.implementation:}")
    private String storageImpl;

    @Value("${dina.fileStorage.root:}")
    private String storageRoot;

    private static final String MARKER_KEY = "no_xlmeta_marker";

    private final MigrationHintsService migrationHintsService;

    public StartupChecks(MigrationHintsService migrationHintsService) {
        this.migrationHintsService = migrationHintsService;
    }

    /**
     * Handles application startup checks for the file system storage
     * implementation.
     * 
     * This method is triggered when the application is ready and performs the
     * following checks:
     * <ul>
     * <li>If the storage implementation is configured as "FS" and a storage root is provided, 
     * it scans the file system tree to see if the previous data in that root was erasure coded. 
     * Once the check is successfully completed, a marker is set so the check is only run once.</li>
     * </ul>
     * 
     * Any exceptions that occur during the startup check process are caught and
     * logged as errors,
     * allowing the application to continue running despite check failures.
     * 
     * @EventListener ApplicationReadyEvent.class - This method is invoked
     *                automatically when the
     *                Spring application context is fully initialized and ready
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onAppReady() {

        if (migrationHintsService.hasHint(MARKER_KEY)) {
            log.info("FS startup check already completed, skipping...");
        } else {
            try {
                if ("FS".equalsIgnoreCase(storageImpl) && StringUtils.hasText(storageRoot)) {
                    Path rootPath = Path.of(storageRoot);
                    XlMetaFileDetector detector = new XlMetaFileDetector();
                    java.nio.file.Files.walkFileTree(rootPath, detector);
                    if (!detector.isFoundXlMeta()) {
                        migrationHintsService.saveHint(MARKER_KEY);
                        log.info("FS startup check complete, marker set.");
                    } else {
                        log.warn("FS startup check terminated early (xl.meta found), marker not set.");
                    }
                }
            } catch (Exception e) {
                log.error("FS startup check failed: {}", e.getMessage());
            }
        }
    }
}
