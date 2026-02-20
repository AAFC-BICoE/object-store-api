package ca.gc.aafc.objectstore.api;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import ca.gc.aafc.objectstore.api.service.MigrationHintsService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.lang.reflect.Field;


@ExtendWith(MockitoExtension.class)
class StartupChecksTest {

    @Mock
    private MigrationHintsService migrationHintsService;

    @InjectMocks
    private StartupChecks startupChecks;

    @BeforeEach
    void setup() throws Exception {
        // set private @Value fields via reflection
        Field storageImpl = StartupChecks.class.getDeclaredField("storageImpl");
        storageImpl.setAccessible(true);
        storageImpl.set(startupChecks, "FS");

        Field storageRoot = StartupChecks.class.getDeclaredField("storageRoot");
        storageRoot.setAccessible(true);
        storageRoot.set(startupChecks, System.getProperty("java.io.tmpdir")); // use temp dir
    }

    @Test
    void onAppReady_whenMarkerExists_skips() {
        when(migrationHintsService.hasHint("no_xlmeta_marker"))
            .thenReturn(true);

        startupChecks.onAppReady();

        verify(migrationHintsService, never()).saveHint("no_xlmeta_marker");
    }
}
