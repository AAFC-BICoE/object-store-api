package ca.gc.aafc.objectstore.api;

import ca.gc.aafc.objectstore.api.dto.LicenseDto;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Configuration class with supported licenses loaded from SupportedLicenses.yml
 *
 */
@Configuration
@PropertySource(value = "classpath:SupportedLicenses.yml",
    factory = YamlPropertyLoaderFactory.class)
@ConfigurationProperties
public class SupportedLicensesConfiguration {

  private Map<String, LicenseDto> licenses;

  public Map<String, LicenseDto> getLicenses() {
    return licenses;
  }

  public void setLicenses(Map<String, LicenseDto> licenses) {
    this.licenses = licenses;
  }

}
