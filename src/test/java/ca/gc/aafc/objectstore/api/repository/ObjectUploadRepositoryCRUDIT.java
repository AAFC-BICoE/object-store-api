package ca.gc.aafc.objectstore.api.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.gc.aafc.dina.dto.JsonApiDto;
import ca.gc.aafc.dina.exception.ResourceGoneException;
import ca.gc.aafc.dina.exception.ResourceNotFoundException;
import ca.gc.aafc.dina.jsonapi.JsonApiBulkDocument;
import ca.gc.aafc.dina.jsonapi.JsonApiBulkResourceIdentifierDocument;
import ca.gc.aafc.dina.jsonapi.JsonApiDocument;
import ca.gc.aafc.dina.repository.DinaRepositoryV2;
import ca.gc.aafc.objectstore.api.dto.ObjectUploadDto;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.inject.Inject;

public class ObjectUploadRepositoryCRUDIT extends ObjectStoreModuleBaseRepositoryIT {

  private static final String BASE_URL = "/api/v1/" + ObjectUploadDto.TYPENAME;

  @Inject
  private ObjectUploadResourceRepository objectUploadRepository;

  @Autowired
  private WebApplicationContext wac;

  private MockMvc mockMvc;

  @Autowired
  protected ObjectUploadRepositoryCRUDIT(ObjectMapper objMapper) {
    super(BASE_URL, objMapper);
  }

  @Override
  protected MockMvc getMockMvc() {
    return mockMvc;
  }

  private ObjectUpload createTestObjectUpload() {
    ObjectUpload testObjectUpload = ObjectUploadFactory.newObjectUpload()
      .build();
    objectUploadService.create(testObjectUpload);
    return testObjectUpload;
  }
  
  private void removeTestObjectUpload(ObjectUpload testObjectUpload) {
    objectUploadService.delete(testObjectUpload);
  }
  
  @BeforeEach
  public void setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
  }

  @Test
  public void findObjectUpload_whenNoIDSpecified_returnedWithAllRecords() {
    createTestObjectUpload();
    DinaRepositoryV2.PagedResource<JsonApiDto<ObjectUploadDto>> objectUploadDtos =  objectUploadRepository
      .getAll("");
    assertFalse(objectUploadDtos.resourceList().isEmpty());
  }
  
  @Test
  public void findObjectUpload_whenIdSpecified_returnedWithSingleResult()
    throws ResourceGoneException, ResourceNotFoundException {
    ObjectUpload testObjectUpload = createTestObjectUpload();

    ObjectUploadDto objectUploadDto = objectUploadRepository
        .getOne(testObjectUpload.getFileIdentifier(), null).getDto();
    assertNotNull(objectUploadDto);
    assertEquals(testObjectUpload.getFileIdentifier(), objectUploadDto.getFileIdentifier());
    assertNotNull(objectUploadDto.getCreatedOn());
    assertEquals(testObjectUpload.getCreatedBy(), objectUploadDto.getCreatedBy());
    assertEquals(testObjectUpload.getBucket(), objectUploadDto.getBucket());
    assertEquals(testObjectUpload.getDcType(), objectUploadDto.getDcType());
    assertEquals(testObjectUpload.getIsDerivative(), objectUploadDto.getIsDerivative());

    removeTestObjectUpload(testObjectUpload);
  }

  @Test
  public void batchLoad_PersistedObjectUpload_ReturnsOkayAndBody() throws Exception {
    ObjectUpload testObjectUpload1 = createTestObjectUpload();
    ObjectUpload testObjectUpload2 = createTestObjectUpload();

    var bulkLoadDocument = JsonApiBulkResourceIdentifierDocument.builder();
    bulkLoadDocument.addData(JsonApiDocument.ResourceIdentifier.builder()
      .type(ObjectUploadDto.TYPENAME)
      .id(testObjectUpload2.getUuid())
      .build());

    MvcResult mvcResult = sendBulkLoad(bulkLoadDocument.build());

    JsonApiBulkDocument returnedDoc = objMapper.readValue(mvcResult.getResponse().getContentAsString(), JsonApiBulkDocument.class);

    assertEquals(1, returnedDoc.getData().size());
    assertEquals(testObjectUpload2.getUuid(), returnedDoc.getData().getFirst().getId());

    removeTestObjectUpload(testObjectUpload1);
    removeTestObjectUpload(testObjectUpload2);
  }

}
