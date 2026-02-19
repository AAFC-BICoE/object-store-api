package ca.gc.aafc.objectstore.api;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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

    @EventListener(ApplicationReadyEvent.class)
    public void onAppReady() {
        try {
            if ("FS".equalsIgnoreCase(storageImpl) && StringUtils.hasText(storageRoot)) {
                Path rootPath = Path.of(storageRoot);
                XlMetaFileDetector detector = new XlMetaFileDetector();
                java.nio.file.Files.walkFileTree(rootPath, detector);
            }
        } catch (Exception e) {
            log.error("FS startup check failed: {}", e.getMessage());
        }
    }
}
