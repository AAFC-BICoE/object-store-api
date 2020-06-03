package ca.gc.aafc.objectstore.api;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import ca.gc.aafc.objectstore.api.entities.DcType;

/**
 * Configuration class related to mapping between dcFormat and dcType.
 *
 */
@Configuration
@PropertySource(value = "classpath:MediaTypeToDcType.yml", factory = YamlPropertyLoaderFactory.class)
@ConfigurationProperties
public class MediaTypeToDcTypeConfiguration {

  private LinkedHashMap<DcType, LinkedList<Pattern>> toDcTypePatterns = new LinkedHashMap<>();
  
  public LinkedHashMap<DcType, LinkedList<Pattern>> getToDcType() {
    return toDcTypePatterns;
  }

  public void setToDcType(LinkedHashMap<String, LinkedList<String>> toDcType) {
    computePatterns(toDcType);
  }
  
  private void computePatterns(LinkedHashMap<String, LinkedList<String>> toDcType) {
    toDcTypePatterns.clear();
    for (Entry<String, LinkedList<String>> entry : toDcType.entrySet()) {
      LinkedList<Pattern> patternList = new LinkedList<>();
      toDcTypePatterns.put(DcType.fromValue(entry.getKey()).orElseThrow(() -> new IllegalArgumentException(entry.getKey() + " is not a valid valud for DcType")), patternList);
      for (String currPattern : entry.getValue()) {
        patternList.add(Pattern.compile(currPattern));
      }
    }
  }
    
}
