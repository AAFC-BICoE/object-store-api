package ca.gc.aafc.objectstore.api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import ca.gc.aafc.objectstore.api.DinaAuthenticatedUserConfig;
import ca.gc.aafc.objectstore.api.dto.ObjectUploadDto;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.respository.ObjectUploadResourceRepository;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;

@ActiveProfiles("test")
public class ObjectUploadRepositoryCRUDIT extends BaseRepositoryTest {
  
  @Inject
  private ObjectUploadResourceRepository objectUploadRepository;
  
  private ObjectUpload testObjectUpload;

  private void createTestObjectUpload(ObjectUpload objectUpload) {
    if(objectUpload == null) {
      testObjectUpload = ObjectUploadFactory.newObjectUpload()
        .build();
      persist(testObjectUpload);
    }else {
      persist(objectUpload); 
    }    
  }
  
  @BeforeEach
  public void setup(){ 
    createTestObjectUpload(null);    
  }  

  @Test
  public void findObjectUpload_whenNoIDSpecified_returnedWithAllRecords() {
    ObjectUpload.ObjectUploadBuilder builder =  testObjectUpload.toBuilder();
    ObjectUpload newTestObjectUpload = builder.createdBy("ee").build();
    
    createTestObjectUpload(newTestObjectUpload);
    
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
    assertEquals(testObjectUpload.getCreatedOn(), objectUploadDto.getCreatedOn());
    assertEquals(testObjectUpload.getCreatedBy(), objectUploadDto.getCreatedBy());
  }    
}
