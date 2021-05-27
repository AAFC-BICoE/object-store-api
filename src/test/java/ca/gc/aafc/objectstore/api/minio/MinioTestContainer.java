package ca.gc.aafc.objectstore.api.minio;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

public class MinioTestContainer extends GenericContainer<MinioTestContainer> {

  private static final String MINIO_ACCESS_KEY = "MINIO_ACCESS_KEY";
  private static final String MINIO_SECRET_KEY = "MINIO_SECRET_KEY";
  public static final String ACCESS_KEY = "minio";
  public static final String SECRET_KEY = "minio123";
  public static final String COMMAND = "server /data";
  private static final String HEALTH_ENDPOINT = "/minio/health/ready";
  public static final int PORT = 9000;


  public MinioTestContainer(String image) {
    super(DockerImageName.parse(image));
    this.withEnv(MINIO_ACCESS_KEY, ACCESS_KEY);
    this.withEnv(MINIO_SECRET_KEY, SECRET_KEY);
    this.withCommand(COMMAND);
    this.addFixedExposedPort(PORT, PORT);
    this.setWaitStrategy(new HttpWaitStrategy()
      .forPort(PORT)
      .forPath(HEALTH_ENDPOINT)
      .withStartupTimeout(Duration.ofMinutes(1)));
  }

}
