package ca.gc.aafc.objectstore.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

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
    @DurationUnit(ChronoUnit.SECONDS)
    private final Duration duration;
  }

}
