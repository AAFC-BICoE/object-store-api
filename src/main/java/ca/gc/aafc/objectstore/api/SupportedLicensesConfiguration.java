package ca.gc.aafc.objectstore.api;

import ca.gc.aafc.objectstore.api.entities.License;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Maps entries in SupportedLicenses.yml to License objects for use in repository
 * 
 * @author chinniahs
 */
@Configuration
@PropertySource(value = "classpath:SupportedLicenses.yml", 
    factory = YamlPropertyLoaderFactory.class)
@ConfigurationProperties
public class SupportedLicensesConfiguration {

  private List<License> licenses = new ArrayList<>();

  @Bean
  public List<License> getLicenses() {
    return this.licenses;
  }

  public void setLicenses(LinkedHashMap<String, LinkedList<String>> licenses) {
    computeLicenses(licenses);
  }

  /**
   * Helper method for parsing name, "url", and "titles" attributes from yml and storing them in
   * License objects.
   * 
   * @param toLicenses set by Spring at runtime
   */
  private void computeLicenses(LinkedHashMap<String, LinkedList<String>> toLicenses) {
    licenses.clear();
    License currentLicense = new License();
    String previousLicenseName = "";
    for (Entry<String, LinkedList<String>> entry : toLicenses.entrySet()) {
      String currentLicenseName = entry.getKey().split("\\.")[0];
      // check if the current entry belongs to the same license as the last entry
      if (!Objects.equals(previousLicenseName, currentLicenseName)) {
        // add last license object to list
        if (currentLicense.getName() != null) {
          licenses.add(currentLicense);
        }
        currentLicense = new License();
        currentLicense.setName(currentLicenseName);
      }
      if (entry.getKey().contains("url")) {
        currentLicense.setUrl(entry.getValue().getFirst());
      } else if (entry.getKey().contains("titles")) {
        currentLicense.addTitle(entry.getKey().split("\\.")[2], entry.getValue().getFirst());
      }
      previousLicenseName = currentLicenseName;
    }
    // add the last-parsed license
    licenses.add(currentLicense);
  }
}
