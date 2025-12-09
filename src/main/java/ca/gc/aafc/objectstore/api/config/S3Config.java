package ca.gc.aafc.objectstore.api.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "s3")
@RequiredArgsConstructor
@Setter
@Getter
public class S3Config {
  private String endpoint;
  private String accessKey;
  private String secretKey;

  private Integer port;

  /**
   * Return the endpoint with the port value (if provided)
   * @return
   */
  public String getEndpoint() {
    if (port != null) {
      return endpoint + ":" + port;
    }
    return endpoint;
  }
}
