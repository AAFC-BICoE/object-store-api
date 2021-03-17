package ca.gc.aafc.objectstore.api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.gc.aafc.objectstore.api.dto.ObjectUploadDto;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.respository.ObjectUploadResourceRepository;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;

public class ObjectUploadRepositoryCRUDIT extends BaseRepositoryTest {
  
  @Inject
  private ObjectUploadResourceRepository objectUploadRepository;
  
  private ObjectUpload testObjectUpload;

  private ObjectUpload createTestObjectUpload() {
    testObjectUpload = ObjectUploadFactory.newObjectUpload()
      .build();
    persist(testObjectUpload);
    return testObjectUpload;
  }
  
  private void removeTestObjectUpload() {
    service.deleteById(ObjectUpload.class, testObjectUpload.getId());
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
    
    assertEquals(2, objectUploadDtos.size());
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
