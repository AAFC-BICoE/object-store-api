package ca.gc.aafc.objectstore.api.minio;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Objects;

public class MinioTestContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  private static MinioTestContainer minioTestContainer;

  @Override
  public void initialize(ConfigurableApplicationContext ctx) {
    ConfigurableEnvironment env = ctx.getEnvironment();
    if (!Objects.equals(env.getProperty("embedded.minio.enabled"), "false")) {
      if (minioTestContainer == null) {
        String imageName = env.getProperty("embedded.minio.image");
        if (StringUtils.isBlank(imageName)) {
          throw new IllegalArgumentException("you must supply the embedded.minio.image property");
        }
        minioTestContainer = new MinioTestContainer(imageName);
        minioTestContainer.start();
      }
      TestPropertyValues.of(
        "minio.port:" + minioTestContainer.getMappedPort()
      ).applyTo(env);
    }
  }

}
