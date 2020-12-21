package ca.gc.aafc.objectstore.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "objectstore")
@ConstructorBinding
@RequiredArgsConstructor
@Getter
public class DefaultValueConfiguration {

  private final String defaultLicenceURL;
  private final String defaultCopyright;
  private final String defaultCopyrightOwner;
  private final String defaultUsageTerms;

}
