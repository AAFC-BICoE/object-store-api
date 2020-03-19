package ca.gc.aafc.objectstore.api.rest;

import static io.restassured.RestAssured.given;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.gc.aafc.objectstore.api.BaseHttpIntegrationTest;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import io.crnk.core.engine.http.HttpStatus;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class DcTypeJsonSerializationIT extends BaseHttpIntegrationTest {

  private static final String TEST_ENDPOINT = "/api/v1/object-subtype";

  @BeforeEach
  public void setup() {
    RestAssured.port = testPort;
  }

  @AfterEach
  public void tearDown() {
    runInNewTransaction(em -> {
      CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
      CriteriaDelete<ObjectSubtype> query = criteriaBuilder.createCriteriaDelete(ObjectSubtype.class);
      Root<ObjectSubtype> root = query.from(ObjectSubtype.class);
      query.where(criteriaBuilder.isNotNull(root.get("dcType")));
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
        .contentType(BaseJsonApiIntegrationTest.JSON_API_CONTENT_TYPE)
        .body(getPostBody(dcType))
        .when()
        .post(TEST_ENDPOINT);
  }

  private static String getPostBody(String dcType) {
    return "{ \"data\": { \"type\": \"object-subtype\", \"attributes\": { \"dcType\":\"" 
      + dcType
      + "\", \"acSubtype\":\"thumbnail\" } } }";
  }

}