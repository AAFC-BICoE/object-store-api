package ca.gc.aafc.objectstore.api;

import ca.gc.aafc.dina.DinaBaseApiAutoConfiguration;
import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.mapper.JpaDtoMapper;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.respository.DtoEntityMapping;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashMap;
import java.util.concurrent.Executor;

@Configuration
@EntityScan("ca.gc.aafc.objectstore.api.entities")
@ComponentScan(basePackageClasses = DinaBaseApiAutoConfiguration.class)
@ImportAutoConfiguration(DinaBaseApiAutoConfiguration.class)
@EnableAsync
public class MainConfiguration implements AsyncConfigurer {

  @Bean
  @ConditionalOnMissingBean
  public MinioClient initMinioClient(
    @Value("${minio.scheme:}") String protocol,
    @Value("${minio.host:}") String host,
    @Value("${minio.port:}") int port,
    @Value("${minio.accessKey:}") String accessKey,
    @Value("${minio.secretKey:}") String secretKey
  ) {
    String endpoint = protocol + "://" + host;
    return MinioClient.builder()
      .endpoint(endpoint, port, false)
      .credentials(accessKey, secretKey).build();
  }

  /**
   * Configures DTO-to-Entity mappings.
   *
   * @return the DtoJpaMapper
   */
  @Bean
  public JpaDtoMapper dtoJpaMapper(BaseDAO baseDAO) {
    return new JpaDtoMapper(
      DtoEntityMapping.getDtoToEntityMapping(ObjectStoreMetadataDto.class),
      new HashMap<>()
    );
  }

  @Override
  public Executor getAsyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);
    executor.setMaxPoolSize(15);
    executor.setQueueCapacity(1000);
    executor.setThreadNamePrefix("AsyncExecutor-");
    executor.initialize();
    return executor;
  }

}
