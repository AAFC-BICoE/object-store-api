package ca.gc.aafc.objectstore.api.crud;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import ca.gc.aafc.dina.testsupport.DatabaseSupportService;
import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.objectstore.api.ObjectStoreApiLauncher;

/**
 * The class is ported from seqdb.dbi with below changes, will be moved to a
 * common package later.
 * 
 * 1. Remove the TestConfig as the entitiy scan is now on application launcher,
 * no need to use another application context.
 * 
 * Base class for CRUD-based Integration tests. The main purpose is to ensure
 * all entities can be saved/loaded/deleted from a database.
 * 
 * This base class with run a single test (see testCRUDOperations) to control to
 * order of testing of save/find/remove.
 * 
 */
@SpringBootTest(
  classes = ObjectStoreApiLauncher.class,
  properties = "spring.config.additional-location=classpath:/application-test.yml"
)
@Transactional
@ContextConfiguration(initializers = { PostgresTestContainerInitializer.class })
public abstract class BaseEntityCRUDIT {

  @Inject
  protected DatabaseSupportService service;

  /**
   * Runs the three main CRUD methods while performing a transaction for each
   * test.
   * 
   * @throws InstantiationException
   * @throws IllegalAccessException
   */
  @Test
  public void testCRUDOperations() throws InstantiationException, IllegalAccessException {
    testSave();
    testFind();
    testRemove();
  }

  public abstract void testSave();

  public abstract void testFind();

  public abstract void testRemove();

}
