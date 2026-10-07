package ca.gc.aafc.objectstore.api.storage;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import ca.gc.aafc.objectstore.api.config.S3Config;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Makes sure {@link S3FileManagement} is only created in S3 mode, like the other storage beans.
 */
public class S3FileManagementConditionTest {

  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
    .withBean(S3Config.class)
    .withUserConfiguration(S3FileManagement.class);

  @Test
  public void whenS3_beanCreated() {
    contextRunner.withPropertyValues("dina.fileStorage.implementation=S3")
      .run(ctx -> assertThat(ctx).hasSingleBean(S3FileManagement.class));
    // case-insensitive, same as OpenDALFileStorage
    contextRunner.withPropertyValues("dina.fileStorage.implementation=s3")
      .run(ctx -> assertThat(ctx).hasSingleBean(S3FileManagement.class));
  }

  @Test
  public void whenNotS3_noBean() {
    contextRunner.withPropertyValues("dina.fileStorage.implementation=FS")
      .run(ctx -> assertThat(ctx).hasNotFailed().doesNotHaveBean(S3FileManagement.class));
    contextRunner.withPropertyValues("dina.fileStorage.implementation=MINIO")
      .run(ctx -> assertThat(ctx).hasNotFailed().doesNotHaveBean(S3FileManagement.class));
    // property not set: no placeholder resolution failure
    contextRunner
      .run(ctx -> assertThat(ctx).hasNotFailed().doesNotHaveBean(S3FileManagement.class));
  }
}
