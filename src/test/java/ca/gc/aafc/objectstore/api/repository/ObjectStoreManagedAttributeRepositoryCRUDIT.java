package ca.gc.aafc.objectstore.api.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.gc.aafc.dina.exception.ResourceGoneException;
import ca.gc.aafc.dina.exception.ResourceNotFoundException;
import ca.gc.aafc.dina.jsonapi.JsonApiDocument;
import ca.gc.aafc.dina.jsonapi.JsonApiDocuments;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.dina.vocabulary.TypedVocabularyElement;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreManagedAttributeDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreManagedAttribute;
import ca.gc.aafc.objectstore.api.testsupport.factories.MultilingualDescriptionFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreManagedAttributeFactory;
import ca.gc.aafc.objectstore.api.testsupport.fixtures.ObjectStoreManagedAttributeFixture;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.inject.Inject;

public class ObjectStoreManagedAttributeRepositoryCRUDIT extends ObjectStoreModuleBaseRepositoryIT {

  @Autowired
  private WebApplicationContext wac;

  private MockMvc mockMvc;

  @Inject
  private ObjectStoreManagedAttributeResourceRepository managedResourceRepository;
  
  private ObjectStoreManagedAttribute testManagedAttribute;

  private final static String DINA_USER_NAME = "dev";

  @Autowired
  protected ObjectStoreManagedAttributeRepositoryCRUDIT(String baseUrl,
                                                        ObjectMapper objMapper) {
    super(baseUrl, objMapper);
  }

  private ObjectStoreManagedAttribute createTestManagedAttribute() throws JsonProcessingException {
    testManagedAttribute = ObjectStoreManagedAttributeFactory.newManagedAttribute()
        .acceptedValues(new String[] { "dosal" })
        .multilingualDescription(MultilingualDescriptionFactory.newMultilingualDescription().build())
        .build();

    return managedAttributeService.create(testManagedAttribute);
  }

  @Override
  protected MockMvc getMockMvc() {
    return mockMvc;
  }

  @BeforeEach
  public void setup() throws JsonProcessingException { 
    createTestManagedAttribute();
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
  }  

  @Test
  public void findManagedAttribute_whenNoFieldsAreSelected_manageAttributeReturnedWithAllFields()
    throws ResourceGoneException, ResourceNotFoundException {

    ObjectStoreManagedAttributeDto managedAttributeDto = managedResourceRepository.getOne(
      testManagedAttribute.getUuid(), null
    ).getDto();

    assertNotNull(managedAttributeDto);
    assertEquals(testManagedAttribute.getUuid(), managedAttributeDto.getUuid());
    assertArrayEquals(testManagedAttribute.getAcceptedValues(),
        managedAttributeDto.getAcceptedValues());
    assertEquals(testManagedAttribute.getVocabularyElementType(),
        managedAttributeDto.getVocabularyElementType());
    assertEquals(testManagedAttribute.getName(), managedAttributeDto.getName());
    assertEquals(testManagedAttribute.getMultilingualDescription().getDescriptions().getFirst(),
        managedAttributeDto.getMultilingualDescription().getDescriptions().getFirst());
  }

  @Test
  public void create_WithAuthenticatedUser_SetsCreatedBy()
    throws ResourceGoneException, ResourceNotFoundException {
    ObjectStoreManagedAttributeDto ma = ObjectStoreManagedAttributeFixture
        .newObjectStoreManagedAttribute();
    ma.setName("name");
    ma.setVocabularyElementType(TypedVocabularyElement.VocabularyElementType.STRING);
    ma.setAcceptedValues(new String[] { "dosal" });

    JsonApiDocument docToCreate = JsonApiDocuments.createJsonApiDocument(
      null, ObjectStoreManagedAttributeDto.TYPENAME,
      JsonAPITestHelper.toAttributeMap(ma)
    );
    var createdDto = managedResourceRepository.create(docToCreate, null);

    ObjectStoreManagedAttributeDto result = managedResourceRepository.getOne(
      createdDto.getDto().getUuid(), null
    ).getDto();
    assertEquals(DINA_USER_NAME, result.getCreatedBy());
  }

  @Test
  void findOneByKey_whenKeyProvided_managedAttributeFetched() throws Exception {
    ObjectStoreManagedAttributeDto newAttribute = ObjectStoreManagedAttributeFixture
        .newObjectStoreManagedAttribute();
    newAttribute.setName("Object Store Attribute 1");
    newAttribute.setVocabularyElementType(TypedVocabularyElement.VocabularyElementType.INTEGER);

    JsonApiDocument docToCreate = JsonApiDocuments.createJsonApiDocument(
      null, ObjectStoreManagedAttributeDto.TYPENAME,
      JsonAPITestHelper.toAttributeMap(newAttribute)
    );
    var createdDto = managedResourceRepository.create(docToCreate, null);

    // Fetch using the key instead of the UUID:
    var findOneResponse = sendGet("object_store_attribute_1");
    JsonApiDocument apiDoc = toJsonApiDocument(findOneResponse);

    assertEquals(createdDto.getDto().getUuid(), apiDoc.getId());
  }

}
