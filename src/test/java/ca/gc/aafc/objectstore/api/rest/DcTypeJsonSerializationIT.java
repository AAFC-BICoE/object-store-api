package ca.gc.aafc.objectstore.api.rest;

import static io.restassured.RestAssured.given;

import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;

import com.google.common.collect.ImmutableMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import ca.gc.aafc.dina.testsupport.factories.TestableEntityFactory;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.ObjectStoreApiLauncher;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import io.crnk.core.engine.http.HttpStatus;
import io.restassured.response.Response;

@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  classes = ObjectStoreApiLauncher.class,
  properties = "dev-user.enabled=true"
)
public class DcTypeJsonSerializationIT extends BaseIntegrationTest {

  private static final String RESOURCE_UNDER_TEST = "object-subtype";
  private static final String JSON_API_CONTENT_TYPE = "application/vnd.api+json";
  private static final String API_BASE_PATH = "/api/v1/";
  private static final String AC_SUB_TYPE = TestableEntityFactory.generateRandomNameLettersOnly(5);

  @LocalServerPort
  protected int testPort;

  @AfterEach
  public void tearDown() {
    service.runInNewTransaction(em -> {
      CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
      CriteriaDelete<ObjectSubtype> query = criteriaBuilder.createCriteriaDelete(ObjectSubtype.class);
      Root<ObjectSubtype> root = query.from(ObjectSubtype.class);
      query.where(criteriaBuilder.equal(root.get("acSubtype"), AC_SUB_TYPE));
      em.createQuery(query).executeUpdate();
    });
  }

  @Test
  public void ValidDcType_ReturnsCreated_201() {
    Response response = sendPostWithDcType("image");
    response.then().statusCode(HttpStatus.CREATED_201);
  }

  @Test
  public void InValidDcType_ReturnsBadRequest_400() {
    Response response = sendPostWithDcType("Invalid-type");
    response.then().statusCode(HttpStatus.BAD_REQUEST_400);
  }

  private Response sendPostWithDcType(String dcType) {
    return given()
        .header("crnk-compact", "true")
        .port(testPort)
        .basePath(API_BASE_PATH)
        .contentType(JSON_API_CONTENT_TYPE)
        .body(getPostBody(dcType))
        .when()
        .post(RESOURCE_UNDER_TEST);
  }

  private static Map<String, Object> getPostBody(String dcType) {
    ImmutableMap.Builder<String, Object> objAttribMap = new ImmutableMap.Builder<>();
    objAttribMap.put("dcType", dcType);
    objAttribMap.put("acSubtype", AC_SUB_TYPE);

    return JsonAPITestHelper.toJsonAPIMap(RESOURCE_UNDER_TEST, objAttribMap.build(), null, null);
  }

}