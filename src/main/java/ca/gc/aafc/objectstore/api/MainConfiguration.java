package ca.gc.aafc.objectstore.api;

import ca.gc.aafc.dina.DinaBaseApiAutoConfiguration;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.WebContentInterceptor;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Configuration
@EntityScan("ca.gc.aafc.objectstore.api.entities")
@ComponentScan(basePackageClasses = DinaBaseApiAutoConfiguration.class)
@ImportAutoConfiguration(DinaBaseApiAutoConfiguration.class)
@EnableAsync
public class MainConfiguration implements AsyncConfigurer, WebMvcConfigurer {

  private static final String[] CACHE_CONTROLLED_PATHS = new String[]{"/api/v1/license", "/api/v1/config"};

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

  @Bean 
  public  WebContentInterceptor getWebContentInterceptor() {
    return new WebContentInterceptor();
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    WebContentInterceptor interceptor = getWebContentInterceptor();
    interceptor.addCacheMapping(CacheControl.maxAge(24, TimeUnit.HOURS)
      .noTransform()
      .mustRevalidate(), CACHE_CONTROLLED_PATHS);
    registry.addInterceptor(interceptor);
  }  

}
