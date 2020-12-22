package ca.gc.aafc.objectstore.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "spring.servlet.multipart")
@ConstructorBinding
@RequiredArgsConstructor
@Getter
public class FileUploadConfiguration {

  private final String maxFileSize;
  private final String maxRequestSize;

}
