package ca.gc.aafc.objecstore.api.repository;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;

public abstract class BaseRepositoryTest extends BaseIntegrationTest {
  
    
  /**
   * Persists an entity.
   * 
   * @param the entity to persist
   */
  protected void persist(Object objectToPersist) {
    save(objectToPersist);
  }
  
  protected void delete(Object objectToPersist) {
    delete(objectToPersist);
  }

}
