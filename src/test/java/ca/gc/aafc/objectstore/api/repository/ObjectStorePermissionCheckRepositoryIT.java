package ca.gc.aafc.objectstore.api.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.gc.aafc.dina.dto.PermissionCheckDto;
import ca.gc.aafc.dina.jsonapi.JsonApiDocument;
import ca.gc.aafc.dina.jsonapi.JsonApiDocuments;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.dina.testsupport.security.WithMockKeycloakUser;

import static com.toedter.spring.hateoas.jsonapi.MediaTypes.JSON_API_VALUE;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;

import jakarta.inject.Inject;
import java.util.Map;

@SpringBootTest(properties = "keycloak.enabled=true")
public class ObjectStorePermissionCheckRepositoryIT extends BaseIntegrationTest {

  @Inject
  private ObjectMapper objectMapper;

  @Value("${dina.apiPrefix:}")
  private String apiPrefix;

  @Inject
  private WebApplicationContext wac;

  private MockMvc mockMvc;

  @BeforeEach
  public void setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
  }

  @Test
  @WithMockKeycloakUser(username="user", groupRole = {"group 1:USER"})
  public void onPermissionCheck_responseReturned() throws Exception {

    JsonApiDocument doc = JsonApiDocuments.createJsonApiDocument(
      null, ObjectStoreMetadataDto.TYPENAME,
      JsonAPITestHelper.toAttributeMap(Map.of("bucket", "group 1"))
    );

    var response = mockMvc.perform(
        post(apiPrefix + "/" + PermissionCheckDto.TYPE_NAME)
          .contentType(JSON_API_VALUE).
          content(objectMapper.writeValueAsString(doc)))
      .andExpect(status().isCreated())
      .andReturn();

    JsonApiDocument returnedDoc =
      objectMapper.readValue(response.getResponse().getContentAsString(), JsonApiDocument.class);

    assertNotNull(returnedDoc.getAttributes());
  }
}
