package ca.gc.aafc.objectstore.api;

import ca.gc.aafc.objectstore.api.entities.DcType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

public class AppStartsIT extends BaseIntegrationTest {
  
  @Inject
  private DefaultValueConfiguration defaultValueConfiguration;
  
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
    Assertions.assertEquals(4, defaultValueConfiguration.getValues().size());
    Assertions.assertNotNull(mediaTypeToDcTypeConfig.getToDcType().get(DcType.IMAGE).get(0));
    Assertions.assertNotNull(supportedLicensesConfiguration.getLicenses().entrySet().iterator().next());
  }

}
