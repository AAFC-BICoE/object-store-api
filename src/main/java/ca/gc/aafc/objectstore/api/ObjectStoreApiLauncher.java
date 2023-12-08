package ca.gc.aafc.objectstore.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import ca.gc.aafc.objectstore.api.config.FileUploadConfiguration;

/**
 * Launches the application.
 */
//CHECKSTYLE:OFF HideUtilityClassConstructor (Configuration class can not have invisible constructor, ignore the check style error for this case)
@SpringBootApplication
@EnableConfigurationProperties(
  value = {DefaultValueConfiguration.class, FileUploadConfiguration.class}
)
public class ObjectStoreApiLauncher {
  public static void main(String[] args) {
    SpringApplication.run(ObjectStoreApiLauncher.class, args);
  }
}
