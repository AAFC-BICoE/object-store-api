package ca.gc.aafc.objectstore.api;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import ca.gc.aafc.objectstore.api.service.MigrationHintsService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;


@ExtendWith(MockitoExtension.class)
class StartupChecksTest {

    @Mock
    private MigrationHintsService migrationHintsService;

    @InjectMocks
    private StartupChecks startupChecks;

    // dedicated storage root since the check walks the entire tree
    @TempDir
    Path tempDir;

    @BeforeEach
    void setup() throws Exception {
        // set private @Value fields via reflection
        setField("storageImpl", "FS");
        setField("storageRoot", tempDir.toString()); // use temp dir
    }

    @Test
    void afterSingletonsInstantiated_whenMarkerExists_skips() {
        when(migrationHintsService.hasHint("no_xlmeta_marker"))
            .thenReturn(true);

        startupChecks.afterSingletonsInstantiated();

        verify(migrationHintsService, never()).saveHint("no_xlmeta_marker");
    }

    @Test
    void afterSingletonsInstantiated_whenNoXlMeta_setsMarker() throws IOException {
        createFile(tempDir.resolve("bucket/ab/cd/abcd1234.txt"));
        when(migrationHintsService.hasHint("no_xlmeta_marker"))
            .thenReturn(false);

        startupChecks.afterSingletonsInstantiated();

        verify(migrationHintsService).saveHint("no_xlmeta_marker");
    }

    @Test
    void afterSingletonsInstantiated_whenXlMetaFound_throws() throws IOException {
        // MinIO erasure coded object: a folder named after the object containing xl.meta
        createFile(tempDir.resolve("bucket/ab/cd/abcd1234.txt/xl.meta"));
        when(migrationHintsService.hasHint("no_xlmeta_marker"))
            .thenReturn(false);

        assertThrows(IllegalStateException.class, () -> startupChecks.afterSingletonsInstantiated());

        verify(migrationHintsService, never()).saveHint("no_xlmeta_marker");
    }

    @Test
    void afterSingletonsInstantiated_whenScanIncomplete_markerNotSet() throws Exception {
        // the root can't be read, same handling as any unreadable entry in the tree
        setField("storageRoot", tempDir.resolve("does-not-exist").toString());
        when(migrationHintsService.hasHint("no_xlmeta_marker"))
            .thenReturn(false);

        assertDoesNotThrow(() -> startupChecks.afterSingletonsInstantiated());

        verify(migrationHintsService, never()).saveHint("no_xlmeta_marker");
    }

    @Test
    void afterSingletonsInstantiated_whenS3_noScan() throws Exception {
        setField("storageImpl", "S3");
        createFile(tempDir.resolve("bucket/ab/cd/abcd1234.txt/xl.meta"));

        assertDoesNotThrow(() -> startupChecks.afterSingletonsInstantiated());

        verify(migrationHintsService, never()).saveHint(anyString());
    }

    private void setField(String name, String value) throws Exception {
        Field field = StartupChecks.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(startupChecks, value);
    }

    private static void createFile(Path file) throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, "dina");
    }
}
