package ca.gc.aafc.objectstore.api;

import static org.junit.Assert.assertNotNull;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.objectstore.api.entities.DcType;

@SpringBootTest(
  classes = ObjectStoreApiLauncher.class,
  properties = "spring.config.additional-location=classpath:/application-test.yml"
)
@ContextConfiguration(initializers = { PostgresTestContainerInitializer.class })
public class AppStartsIT {
  
  @Inject
  private ObjectStoreConfiguration config;
  
  @Inject
  private MediaTypeToDcTypeConfiguration mediaTypeToDcTypeConfig;

  @Inject
  private SupportedLicensesConfiguration supportedLicensesConfiguration;

  /**
   * Tests that the application with embedded Tomcat starts up successfully.
   */
  @Test
  public void startApp_OnStartUp_NoErrorsThrown() {
    
    //Make sure we can load the configuration files
    assertNotNull(config.getDefaultCopyright());
    assertNotNull(mediaTypeToDcTypeConfig.getToDcType().get(DcType.IMAGE).get(0));
    assertNotNull(supportedLicensesConfiguration.getLicenses().entrySet().iterator().next());
  }

}
