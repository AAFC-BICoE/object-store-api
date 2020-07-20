package ca.gc.aafc.objectstore.api.repository;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;

public abstract class BaseRepositoryTest extends BaseIntegrationTest {
  
    
  /**
   * Persists an entity.
   * 
   * @param the entity to persist
   */
  protected void persist(Object objectToPersist) {
    service.save(objectToPersist);
  }

}
