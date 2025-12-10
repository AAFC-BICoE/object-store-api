package ca.gc.aafc.objectstore.api;

import ca.gc.aafc.dina.testsupport.DatabaseSupportService;
import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.objectstore.api.async.AsyncConsumer;
import ca.gc.aafc.objectstore.api.service.DerivativeGenerationService;
import ca.gc.aafc.objectstore.api.service.DerivativeService;
import ca.gc.aafc.objectstore.api.service.ObjectExportService;
import ca.gc.aafc.objectstore.api.service.ObjectStoreManagedAttributeService;
import ca.gc.aafc.objectstore.api.service.ObjectStoreMetaDataService;
import ca.gc.aafc.objectstore.api.service.ObjectSubtypeService;
import ca.gc.aafc.objectstore.api.service.ObjectUploadService;

import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.Future;
import javax.inject.Inject;

@SpringBootTest(classes = ObjectStoreApiLauncher.class, properties = "dev-user.enabled=true")
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
@Transactional
@ContextConfiguration(initializers = {PostgresTestContainerInitializer.class})
@Import(BaseIntegrationTest.ObjectStoreModuleTestConfiguration.class)
public abstract class BaseIntegrationTest {

  // let junit create a temp dir for us
  @TempDir
  static Path rootTempDir;

  // replace the root with that tmp dir to make sure it exists
  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("dina.fileStorage.root", () -> rootTempDir.toAbsolutePath().toString());
  }

  @Inject
  protected DatabaseSupportService service;

  @Inject
  protected DerivativeService derivativeService;

  @Inject
  protected DerivativeGenerationService derivativeGenerationService;

  @Inject
  protected ObjectStoreManagedAttributeService managedAttributeService;

  @Inject
  protected ObjectStoreMetaDataService objectStoreMetaDataService;

  @Inject
  protected ObjectSubtypeService objectSubtypeService;

  @Inject
  protected ObjectUploadService objectUploadService;

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
