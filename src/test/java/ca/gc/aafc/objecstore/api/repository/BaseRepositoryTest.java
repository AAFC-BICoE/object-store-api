package ca.gc.aafc.objecstore.api.repository;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.interfaces.UniqueObj;

public abstract class BaseRepositoryTest extends BaseIntegrationTest {
  
    
  /**
   * Persists an entity.
   * 
   * @param the entity to persist
   */
  protected void persist(UniqueObj objectToPersist) {
    assertNull( objectToPersist.getId());
    entityManager.persist(objectToPersist);
    assertNotNull( objectToPersist.getId());
  }

}
