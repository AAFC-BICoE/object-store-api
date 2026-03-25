package ca.gc.aafc.objectstore.api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.toedter.spring.hateoas.jsonapi.JsonApiConfiguration;

import ca.gc.aafc.dina.DinaBaseApiAutoConfiguration;
import ca.gc.aafc.dina.config.ResourceNameIdentifierConfig;
import ca.gc.aafc.dina.service.JaversDataService;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;

import java.util.concurrent.Executor;

@Configuration
@EntityScan("ca.gc.aafc.objectstore.api.entities")
@ComponentScan(basePackageClasses = DinaBaseApiAutoConfiguration.class)
@ImportAutoConfiguration(DinaBaseApiAutoConfiguration.class)
@EnableAsync
@MapperScan(basePackageClasses = JaversDataService.class)
@EnableScheduling
public class MainConfiguration {

  public static final String DINA_THREAD_POOL_BEAN_NAME = "DinaThreadPoolTaskExecutor";

  @Bean(name = DINA_THREAD_POOL_BEAN_NAME)
  @ConditionalOnMissingBean
  public Executor threadPoolTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);
    executor.setMaxPoolSize(15);
    executor.setQueueCapacity(1000);
    executor.setThreadNamePrefix("AsyncExecutor-");
    executor.initialize();
    return executor;
  }

  @Bean
  public ResourceNameIdentifierConfig provideResourceNameIdentifierConfig() {
    return ResourceNameIdentifierConfig.builder().
      config(ObjectStoreMetadata.class, new ResourceNameIdentifierConfig.ResourceNameConfig("originalFilename", "bucket"))
      .build();
  }

  @Bean
  public JsonApiConfiguration jsonApiConfiguration() {
    return new JsonApiConfiguration()
      .withPluralizedTypeRendered(false)
      .withPageMetaAutomaticallyCreated(false)
      .withObjectMapperCustomizer(objectMapper -> {
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.registerModule(new JavaTimeModule());
      });
  }
}
