package ca.gc.aafc.objectstore.api.config;

import java.util.concurrent.Executor;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;

import ca.gc.aafc.objectstore.api.MainConfiguration;

/**
 * Usage: add @Import(AsyncOverrideConfig.class) to your test.
 */
@TestConfiguration
public class AsyncOverrideConfig implements AsyncConfigurer {

  @Bean(MainConfiguration.DINA_THREAD_POOL_BEAN_NAME)
  public Executor taskExecutor() {
    return new SyncTaskExecutor();
  }
}
