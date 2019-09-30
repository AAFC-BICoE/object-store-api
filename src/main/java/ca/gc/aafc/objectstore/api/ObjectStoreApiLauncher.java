package ca.gc.aafc.objectstore.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;

import io.minio.MinioClient;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;

/**
 * Launches the application.
 */
//CHECKSTYLE:OFF HideUtilityClassConstructor (Configuration class can not have invisible constructor, ignore the check style error for this case)
@SpringBootApplication
@EntityScan("ca.gc.aafc.objectstore.api.entities")
public class ObjectStoreApiLauncher {

  public static void main(String[] args) {
    SpringApplication.run(ObjectStoreApiLauncher.class, args);
  }

  @Bean
  public MinioClient initMinioClient(
      @Value("${minio.scheme:}") String protocol, 
      @Value("${minio.host:}") String host,
      @Value("${minio.port:}") int port, 
      @Value("${minio.accessKey:}") String accessKey, 
      @Value("${minio.secretKey:}") String secretKey)
      throws InvalidEndpointException, InvalidPortException {
    String endpoint = protocol + "://"+host;
    return new MinioClient(endpoint, port, accessKey, secretKey);
  }  

}
