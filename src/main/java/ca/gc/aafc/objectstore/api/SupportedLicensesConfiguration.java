package ca.gc.aafc.objectstore.api;

import ca.gc.aafc.objectstore.api.dto.LicenseDto;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Configuration class with supported licenses loaded from SupportedLicences.yml
 *
 */
@Configuration
@PropertySource(value = "classpath:SupportedLicences.yml",
    factory = YamlPropertyLoaderFactory.class)
@ConfigurationProperties
public class SupportedLicensesConfiguration {

  private final Map<String, LicenseDto> licences;

  public SupportedLicensesConfiguration(Map<String, LicenseDto> licences) {
    this.licences = licences;
  }

  public Map<String, LicenseDto> getLicences() {
    return licences;
  }
}
