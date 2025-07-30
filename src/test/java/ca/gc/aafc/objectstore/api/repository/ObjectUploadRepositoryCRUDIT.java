package ca.gc.aafc.objectstore.api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.inject.Inject;

import ca.gc.aafc.dina.dto.JsonApiDto;
import ca.gc.aafc.dina.exception.ResourceGoneException;
import ca.gc.aafc.dina.exception.ResourceNotFoundException;
import ca.gc.aafc.dina.repository.DinaRepositoryV2;
import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.gc.aafc.objectstore.api.dto.ObjectUploadDto;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;

public class ObjectUploadRepositoryCRUDIT extends BaseIntegrationTest {
  
  @Inject
  private ObjectUploadResourceRepository objectUploadRepository;
  
  private ObjectUpload testObjectUpload;

  private ObjectUpload createTestObjectUpload() {
    testObjectUpload = ObjectUploadFactory.newObjectUpload()
      .build();
    objectUploadService.create(testObjectUpload);
    return testObjectUpload;
  }
  
  private void removeTestObjectUpload() {
    objectUploadService.delete(testObjectUpload);
  }
  
  @BeforeEach
  public void setup() { 
    createTestObjectUpload();    
  }  
  
  @AfterEach
  public void teardown() { 
    removeTestObjectUpload();    
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
    ObjectUploadDto objectUploadDto = objectUploadRepository
        .getOne(testObjectUpload.getFileIdentifier(), null).getDto();
    assertNotNull(objectUploadDto);
    assertEquals(testObjectUpload.getFileIdentifier(), objectUploadDto.getFileIdentifier());
    assertNotNull(objectUploadDto.getCreatedOn());
    assertEquals(testObjectUpload.getCreatedBy(), objectUploadDto.getCreatedBy());
    assertEquals(testObjectUpload.getBucket(), objectUploadDto.getBucket());
    assertEquals(testObjectUpload.getDcType(), objectUploadDto.getDcType());
    assertEquals(testObjectUpload.getIsDerivative(), objectUploadDto.getIsDerivative());
  }    
}
