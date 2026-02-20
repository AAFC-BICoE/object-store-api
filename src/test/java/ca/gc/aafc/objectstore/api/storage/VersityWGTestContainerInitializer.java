package ca.gc.aafc.objectstore.api.storage;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

public class VersityWGTestContainerInitializer implements
  ApplicationContextInitializer<ConfigurableApplicationContext> {

  private static VersityWGTestContainer s3TestContainer;

  @Override
  public void initialize(ConfigurableApplicationContext ctx) {
    ConfigurableEnvironment env = ctx.getEnvironment();
    if (!Objects.equals(env.getProperty("embedded.minio.enabled"), "false")) {
      if (s3TestContainer == null) {
        String imageName = env.getProperty("embedded.minio.image");
        if (StringUtils.isBlank(imageName)) {
          throw new IllegalArgumentException("you must supply the embedded.minio.image property");
        }
        s3TestContainer = new VersityWGTestContainer("versity/versitygw:latest");
        s3TestContainer.start();
      }
      TestPropertyValues.of(
        "s3.port:" + s3TestContainer.getMappedPort()
      ).applyTo(env);
    }
  }
}
