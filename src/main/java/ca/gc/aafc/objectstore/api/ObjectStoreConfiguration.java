package ca.gc.aafc.objectstore.api;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties(prefix = "objectstore")
public class ObjectStoreConfiguration {
  
  private final String defaultLicenceURL;
  private final String defaultCopyright;
  private final String defaultCopyrightOwner;
  private final String defaultUsageTerms;

  public ObjectStoreConfiguration(String defaultLicenceURL, String defaultCopyright, String defaultCopyrightOwner, String defaultUsageTerms ) {
    this.defaultLicenceURL = defaultLicenceURL;
    this.defaultCopyright = defaultCopyright;
    this.defaultCopyrightOwner = defaultCopyrightOwner;
    this.defaultUsageTerms = defaultUsageTerms;
  }

  public String getDefaultLicenceURL() {
    return defaultLicenceURL;
  }

  public String getDefaultCopyright() {
    return defaultCopyright;
  }
  
  public String getDefaultCopyrightOwner() {
    return defaultCopyrightOwner;
  }

  public String getDefaultUsageTerms() {
    return defaultUsageTerms;
  }

}
