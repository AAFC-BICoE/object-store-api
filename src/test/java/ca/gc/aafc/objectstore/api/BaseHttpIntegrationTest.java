package ca.gc.aafc.objectstore.api;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

/**
 * 
 * The class is  ported from seqdb.api as is, will be moved to a common package later
 * Base class for integration test through browser
 *
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = ObjectStoreApiLauncher.class
)
public abstract class BaseHttpIntegrationTest extends BaseIntegrationTest { 
  @LocalServerPort
  protected int testPort;
  
}
