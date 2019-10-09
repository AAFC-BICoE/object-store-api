package ca.gc.aafc.objectstore.api.crud;

import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import ca.gc.aafc.objectstore.api.testsupport.DBBackedIntegrationTest;


/**
 * The class is ported from seqdb.dbi with below changes, will be moved to a common package later.
 *  
 * 1. Remove the TestConfig as the entitiy scan is now on application launcher,
 *  no need to use another application context.
 * 2. Remove the @ActiveProfiles as the test and local dev are now relying on the postgres in a container, 
 * no need to use another yml file for test.
 * 
 * Base class for CRUD-based Integration tests.
 * The main purpose is to ensure all entities can be saved/loaded/deleted from a database.
 * 
 * This base class with run a single test (see testCRUDOperations) to control to order of testing of save/find/remove.
 * 
 */
@SpringBootTest
@Transactional
public abstract class BaseEntityCRUDIT extends DBBackedIntegrationTest {

  /**
   * Runs the three main CRUD methods while performing a transaction for each test.
   * @throws InstantiationException
   * @throws IllegalAccessException
   */
	@Test
	public void testCRUDOperations() throws InstantiationException, IllegalAccessException {
		testSave();
		testFind();
		testRemove();
	}

	public abstract void testSave() ;
	
	public abstract void testFind() ;

	public abstract void testRemove() ;
	
}
