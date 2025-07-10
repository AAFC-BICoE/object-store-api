package ca.gc.aafc.objectstore.api.repository;

import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.dina.testsupport.repository.MockMvcBasedRepository;
import ca.gc.aafc.objectstore.api.ObjectStoreApiLauncher;
import ca.gc.aafc.objectstore.api.async.AsyncConsumer;
import ca.gc.aafc.objectstore.api.service.ObjectExportService;
import ca.gc.aafc.objectstore.api.service.ObjectStoreManagedAttributeService;

import java.util.Properties;
import java.util.concurrent.Future;
import javax.inject.Inject;

@SpringBootTest(classes = ObjectStoreApiLauncher.class, properties = "dev-user.enabled=true")
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
@Transactional
@ContextConfiguration(initializers = {PostgresTestContainerInitializer.class})
@Import(ObjectStoreModuleBaseRepositoryIT.ObjectStoreModuleTestConfiguration.class)
public abstract class ObjectStoreModuleBaseRepositoryIT extends MockMvcBasedRepository {

  @Inject
  protected ObjectStoreManagedAttributeService managedAttributeService;


  protected ObjectStoreModuleBaseRepositoryIT(String baseUrl,
                                             ObjectMapper objMapper) {
    super(baseUrl, objMapper);
  }

  @TestConfiguration
  public static class ObjectStoreModuleTestConfiguration {
    @Bean
    public BuildProperties buildProperties() {
      Properties props = new Properties();
      props.setProperty("version", "object-store-module-version");
      return new BuildProperties(props);
    }

    @Bean
    public AsyncConsumer<Future<ObjectExportService.ExportResult>> futureExportResultConsumer() {
      return new AsyncConsumer<>();
    }
  }

}
