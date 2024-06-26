package ca.gc.aafc.objectstore.api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.inject.Inject;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.gc.aafc.objectstore.api.dto.ObjectUploadDto;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;

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
    
    ResourceList<ObjectUploadDto> objectUploadDtos =  objectUploadRepository
        .findAll(new QuerySpec(ObjectUploadDto.class));
    
    assertTrue(objectUploadDtos.size() >= 1);
  }
  
  @Test
  public void findObjectUpload_whenIdSpecified_returnedWithSingleResult() {
    ObjectUploadDto objectUploadDto = objectUploadRepository
        .findOne(testObjectUpload.getFileIdentifier(), new QuerySpec(ObjectUploadDto.class));
    assertNotNull(objectUploadDto);
    assertEquals(testObjectUpload.getFileIdentifier(), objectUploadDto.getFileIdentifier());
    assertNotNull(objectUploadDto.getCreatedOn());
    assertEquals(testObjectUpload.getCreatedBy(), objectUploadDto.getCreatedBy());
    assertEquals(testObjectUpload.getBucket(), objectUploadDto.getBucket());
    assertEquals(testObjectUpload.getDcType(), objectUploadDto.getDcType());
    assertEquals(testObjectUpload.getIsDerivative(), objectUploadDto.getIsDerivative());
  }    
}
