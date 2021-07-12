package ca.gc.aafc.objectstore.api.openapi;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.transaction.Transactional;

import org.apache.http.client.utils.URIBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import ca.gc.aafc.dina.testsupport.BaseRestAssuredTest;
import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.dina.testsupport.specs.OpenAPI3Assertions;
import ca.gc.aafc.objectstore.api.DinaAuthenticatedUserConfig;
import ca.gc.aafc.objectstore.api.ObjectStoreApiLauncher;
import ca.gc.aafc.objectstore.api.dto.ObjectSubtypeDto;
import ca.gc.aafc.objectstore.api.entities.DcType;
import lombok.SneakyThrows;

@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  classes = ObjectStoreApiLauncher.class
)
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
@Transactional
@ContextConfiguration(initializers = {PostgresTestContainerInitializer.class})
public class ObjectSubTypeOpenApiIT extends BaseRestAssuredTest {

  private static final String SPEC_HOST = "raw.githubusercontent.com";
  private static final String ROOT_SPEC_PATH = "DINA-Web/object-store-specs/master/schema/object-store-api.yml";
  
  private static final String SCHEMA_NAME = "ObjectSubtype";
  private static final String RESOURCE_UNDER_TEST = "object-subtype";
  private static final String DINA_USER_NAME = DinaAuthenticatedUserConfig.USER_NAME;

  private static final URIBuilder URI_BUILDER = new URIBuilder();

  static {
    URI_BUILDER.setScheme("https");
    URI_BUILDER.setHost(SPEC_HOST);
    URI_BUILDER.setPath(ROOT_SPEC_PATH);
  }

  protected ObjectSubTypeOpenApiIT() {
    super("/api/v1/");
  }

  public static URL getOpenAPISpecsURL() throws URISyntaxException, MalformedURLException {
    return URI_BUILDER.build().toURL();
  }


  @Test
  @SneakyThrows
  void  objectSubtype_SpecValid() {
    ObjectSubtypeDto objectSubtypeDto = buildObjectSubtypeDto();
    OpenAPI3Assertions.assertRemoteSchema(getOpenAPISpecsURL(), SCHEMA_NAME,
    sendPost(RESOURCE_UNDER_TEST, JsonAPITestHelper.toJsonAPIMap(
      RESOURCE_UNDER_TEST, 
      JsonAPITestHelper.toAttributeMap(objectSubtypeDto),
      null,
      null))
      .extract().asString());
  }

  private ObjectSubtypeDto buildObjectSubtypeDto() {
    ObjectSubtypeDto objectSubtype = new ObjectSubtypeDto();
    objectSubtype.setUuid(null);
    objectSubtype.setDcType(DcType.SOUND);
    objectSubtype.setAcSubtype("MusicalNotation");
    objectSubtype.setCreatedBy(DINA_USER_NAME);

    return objectSubtype;
  }
  
}
