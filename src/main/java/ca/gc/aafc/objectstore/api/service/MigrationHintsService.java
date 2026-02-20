package ca.gc.aafc.objectstore.api.service;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;
import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.objectstore.api.entities.MigrationHint;

/**
 * Service for managing migration hints used to track migration progress and state.
 * 
 * This service provides functionality to check for the existence of migration hints
 * and to persist new migration hints to the database. Migration hints are used to
 * store metadata about migration operations, allowing the system to avoid re-processing
 * already migrated data.
 */

@Service 
public class MigrationHintsService{
    
    private final BaseDAO baseDAO;

    /**
     * Constructs a new MigrationHintsService with the provided data access object.
     * 
     * @param baseDAO the data access object used for database operations
     */
    public MigrationHintsService(BaseDAO baseDAO) {
        this.baseDAO = baseDAO;
    }

    /**
     * Checks whether a migration hint with the specified key exists in the database.
     * 
     * @param hintKey the unique identifier of the migration hint to check
     * @return true if a migration hint with the given key exists, false otherwise
     */
    public boolean hasHint(String hintKey) {
        return baseDAO.findOneByProperty(MigrationHint.class, "hintKey", hintKey) != null;
    }

    /**
     * Saves a new migration hint to the database with the specified key.
     * 
     * @param hintKey the unique identifier for the migration hint to be created and saved
     */
    @Transactional
    public void saveHint(String hintKey) {
        MigrationHint hint = new MigrationHint();
        hint.setHintKey(hintKey);
        baseDAO.create(hint);
    }

}
