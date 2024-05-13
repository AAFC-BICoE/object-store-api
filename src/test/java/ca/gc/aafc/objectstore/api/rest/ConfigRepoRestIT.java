package ca.gc.aafc.objectstore.api.rest;

import ca.gc.aafc.dina.testsupport.BaseRestAssuredTest;
import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.objectstore.api.ObjectStoreApiLauncher;
import io.restassured.response.ValidatableResponse;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  classes = ObjectStoreApiLauncher.class,
  properties = "dev-user.enabled=true"
)
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
@Transactional
@ContextConfiguration(initializers = {PostgresTestContainerInitializer.class})
public class ConfigRepoRestIT extends BaseRestAssuredTest {
  protected ConfigRepoRestIT() {
    super("/api/v1/config");
  }

  @Test
  void findAll_ReturnsAllConfigs() {
    ValidatableResponse response = sendGet("");
    response.body("data[0].id", Matchers.equalToObject("file-upload"));
    response.body("data[1].id", Matchers.equalToObject("default-values"));
  }

  @Test
  void findOne_SpecificConfigReturned() {
    ValidatableResponse fileUploadResponse = sendGet("file-upload");
    fileUploadResponse.body("data.id", Matchers.equalToObject("file-upload"));
    fileUploadResponse.body("data.attributes", Matchers.aMapWithSize(2));
    ValidatableResponse defaultValuesResponse = sendGet("default-values");
    defaultValuesResponse.body("data.id", Matchers.equalToObject("default-values"));
    defaultValuesResponse.body("data.attributes.values", Matchers.hasSize(4));
  }
}
