package ca.gc.aafc.objectstore.api.storage;

import java.time.Duration;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.utility.DockerImageName;

/**
 * Lightweight s3 Gateway to expose s3 compliant service backed by filesystem.
 */
public class VersityWGTestContainer extends GenericContainer<VersityWGTestContainer> {

  public static final String ACCESS_KEY = "minio";
  public static final String SECRET_KEY = "minio123";
  public static final int PORT = 7070;

  public VersityWGTestContainer(String image) {
    super(DockerImageName.parse(image));
    this.withExposedPorts(PORT);
    this.withEnv("ROOT_ACCESS_KEY", ACCESS_KEY);
    this.withEnv("ROOT_SECRET_KEY", SECRET_KEY);
    this.withEnv("VGW_HEALTH", "/health");
    this.withEnv("VGW_REGION", "none");
    this.withCommand("posix", "/tmp");
    // Wait for the health check endpoint
    this.setWaitStrategy(new HttpWaitStrategy()
      .forPort(PORT)
      .forPath("/health")
      .withStartupTimeout(Duration.ofMinutes(1)));
    // enable logging of it refuses to start
    //  this.withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("VersityContainer")));
  }

  public int getMappedPort() {
    return getMappedPort(PORT);
  }
}
