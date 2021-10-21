package ca.gc.aafc.objectstore.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "orphan-removal")
@Getter
@Setter
public class OrphanRemovalConfiguration {

  private OrphanRemovalExpirationSetting expiration;

  @ConstructorBinding
  @RequiredArgsConstructor
  @Getter
  public static class OrphanRemovalExpirationSetting {
    private final int seconds;
    private final int minutes;
    private final int hours;
    private final int days;
    private final int weeks;
  }

}
