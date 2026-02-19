package ca.gc.aafc.objectstore.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ca.gc.aafc.objectstore.api.entities.MigrationHint;

public interface MigrationHintsRepository extends JpaRepository<MigrationHint, Integer> {
    boolean existsByHintKey(String hintKey);
}
