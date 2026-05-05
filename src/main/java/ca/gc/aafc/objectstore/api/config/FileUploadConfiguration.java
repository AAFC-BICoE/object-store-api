package ca.gc.aafc.objectstore.api.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "spring.servlet")
//@ConstructorBinding
@RequiredArgsConstructor
@Getter
public class FileUploadConfiguration {

  private final Map<String, Object> multipart;

}
