package ca.gc.aafc.objectstore.api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;
import javax.inject.Inject;

import ca.gc.aafc.dina.exception.ResourceGoneException;
import ca.gc.aafc.dina.exception.ResourceNotFoundException;
import ca.gc.aafc.dina.jsonapi.JsonApiDocument;
import ca.gc.aafc.dina.repository.JsonApiModelAssistant;
import ca.gc.aafc.dina.util.UUIDHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import ca.gc.aafc.objectstore.api.dto.ObjectSubtypeDto;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectSubtypeFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectSubtypeRepositoryCRUDIT extends ObjectStoreModuleBaseRepositoryIT {

  private static final String BASE_URL = "/api/v1/" + ObjectSubtypeDto.TYPENAME;
  private final static String DINA_USER_NAME = "dev";

  @Inject
  private ObjectSubtypeResourceRepository objectSubtypeRepository;

  @Autowired
  private WebApplicationContext wac;

  private MockMvc mockMvc;

  @Autowired
  protected ObjectSubtypeRepositoryCRUDIT(ObjectMapper objMapper) {
    super(BASE_URL, objMapper);
  }

  @Override
  protected MockMvc getMockMvc() {
    return mockMvc;
  }

  @BeforeEach
  public void setup() throws JsonProcessingException {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
  }

  private ObjectSubtype createTestAcSubtype() {
    ObjectSubtype testObjectSubtype = ObjectSubtypeFactory.newObjectSubtype()
      .build();
    return objectSubtypeService.create(testObjectSubtype);
  }

  @Test
  public void findAcSubtype_whenNoFieldsAreSelected_acSubtypeReturnedWithAllFields()
    throws ResourceGoneException, ResourceNotFoundException {

    ObjectSubtype testObjectSubtype = createTestAcSubtype();

    ObjectSubtypeDto objectSubtypeDto = objectSubtypeRepository
      .getOne(testObjectSubtype.getUuid(), "").getDto();

    assertNotNull(objectSubtypeDto);
    assertEquals(testObjectSubtype.getUuid(), objectSubtypeDto.getUuid());
    assertEquals(testObjectSubtype.getAcSubtype(), objectSubtypeDto.getAcSubtype());
    assertEquals(testObjectSubtype.getDcType(), objectSubtypeDto.getDcType());
    assertEquals(testObjectSubtype.getCreatedBy(), objectSubtypeDto.getCreatedBy());
  }
  
  @Test
  public void create_WithAuthenticatedUser_SetsCreatedBy()
      throws ResourceGoneException, ResourceNotFoundException {

    ObjectSubtypeDto os = new ObjectSubtypeDto();
    os.setUuid(UUIDHelper.generateUUIDv7());
    os.setAcSubtype("test subtype".toUpperCase());
    os.setDcType(DcType.IMAGE);

    JsonApiDocument docToCreate = dtoToJsonApiDocument(os);
    var createdDto = objectSubtypeRepository.onCreate(docToCreate);

    UUID uuid = JsonApiModelAssistant.extractUUIDFromRepresentationModelLink(createdDto);
    ObjectSubtypeDto result = objectSubtypeRepository.getOne(uuid, "").getDto();
    assertEquals(DINA_USER_NAME, result.getCreatedBy());
  }
}
