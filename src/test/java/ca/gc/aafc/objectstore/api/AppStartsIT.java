package ca.gc.aafc.objectstore.api;

import static org.junit.Assert.assertNotNull;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import ca.gc.aafc.objectstore.api.entities.DcType;

@SpringBootTest(classes = ObjectStoreApiLauncher.class)
@ActiveProfiles("test")
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
