package ca.gc.aafc.objectstore.api.entities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;

@JsonApiResource(type = "licenses")
public class License {

  @JsonApiId
  private String name;

  @JsonProperty
  private String url;

  @JsonProperty
  private Map<String, String> titles = new HashMap<>();

  /* index constants for titles */
  public static int LANG = 0;
  public static int NAME = 1;

  public License() {
  }

<<<<<<< HEAD
  /**
   * Complete constructor.
   * 
   * @param name   the identifier of the license
   * @param url    link to license
   * @param titles map of language to title pairs
   */
=======
>>>>>>> branch 'Feature_19668_Add_configuration_file_for_list_of_supported_licenses' of https://github.com/AAFC-BICoE/object-store-api.git
  public License(String name, String url, Map<String, String> titles) {
    this.setName(name);
    this.setUrl(url);
    this.setTitles(titles);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Map<String, String> getTitles() {
    return titles;
  }

  public void setTitles(Map<String, String> titles) {
    this.titles = titles;
  }

  public void addTitle(String lang, String title) {
    titles.put(lang, title);
  }
}
