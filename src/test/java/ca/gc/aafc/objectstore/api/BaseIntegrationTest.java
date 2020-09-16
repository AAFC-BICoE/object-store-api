package ca.gc.aafc.objectstore.api;

import javax.inject.Inject;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import ca.gc.aafc.dina.testsupport.DatabaseSupportService;
import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
  classes = ObjectStoreApiLauncher.class,
  properties = "spring.config.additional-location=classpath:/application-test.yml"
)
@Transactional
@ContextConfiguration(initializers = { PostgresTestContainerInitializer.class })
public abstract class BaseIntegrationTest {

  @Inject
  protected DatabaseSupportService service;

}
