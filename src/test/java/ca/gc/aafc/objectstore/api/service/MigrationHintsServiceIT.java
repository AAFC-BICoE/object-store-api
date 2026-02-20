package ca.gc.aafc.objectstore.api.service;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ca.gc.aafc.objectstore.api.BaseIntegrationTest;

@Transactional
class MigrationHintsServiceIT extends BaseIntegrationTest {

    @Autowired
    private MigrationHintsService migrationHintsService;

    @Test
    void hasHint_whenHintMissing_returnsFalse() {
        assertFalse(migrationHintsService.hasHint("nonexistent_key"));
    }

    @Test
    void addHint_thenHasHint_returnsTrue() {
        migrationHintsService.saveHint("test_key");
        assertTrue(migrationHintsService.hasHint("test_key"));
    }
}
