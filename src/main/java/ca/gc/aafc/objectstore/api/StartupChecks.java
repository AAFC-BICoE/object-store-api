package ca.gc.aafc.objectstore.api;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import ca.gc.aafc.objectstore.api.entities.MigrationHint;
import ca.gc.aafc.objectstore.api.repository.MigrationHintsRepository;
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
    
    private static final String MARKER_KEY = "fs_startup_check_complete";

    private final MigrationHintsRepository migrationHintsRepository;

    public StartupChecks(MigrationHintsRepository migrationHintsRepository) {
        this.migrationHintsRepository = migrationHintsRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onAppReady() {

        if (migrationHintsRepository.existsByHintKey(MARKER_KEY)) {
            log.info("FS startup check already completed, skipping...");
            return;
        }

        try {
            if ("FS".equalsIgnoreCase(storageImpl) && StringUtils.hasText(storageRoot)) {
                Path rootPath = Path.of(storageRoot);
                XlMetaFileDetector detector = new XlMetaFileDetector();
                java.nio.file.Files.walkFileTree(rootPath, detector);
                if (!detector.isFoundXlMeta()) {
                    MigrationHint marker = new MigrationHint();
                    marker.setHintKey(MARKER_KEY);
                    migrationHintsRepository.save(marker);
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
