package ca.gc.aafc.objecstore.api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.gc.aafc.objectstore.api.dto.ObjectSubtypeDto;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import ca.gc.aafc.objectstore.api.respository.ObjectSubtypeResourceRepository;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectSubtypeFactory;
import io.crnk.core.queryspec.QuerySpec;

public class ObjectSubtypeRepositoryCRUDIT extends BaseRepositoryTest {
  
  @Inject
  private ObjectSubtypeResourceRepository objectSubtypeRepository;
  
  private ObjectSubtype testObjectSubtype;
  
  private ObjectSubtype createTestAcSubtype() {
    testObjectSubtype = ObjectSubtypeFactory.newObjectSubtype()
        .acSubtype("drawing")
        .build();

    persist(testObjectSubtype);
    return testObjectSubtype;
  }
  
  @BeforeEach
  public void setup(){ 
    createTestAcSubtype();    
  }  

  @Test
  public void findAcSubtype_whenNoFieldsAreSelected_acSubtypeReturnedWithAllFields() {
    ObjectSubtypeDto objectSubtypeDto = objectSubtypeRepository
        .findOne(testObjectSubtype.getUuid(), new QuerySpec(ObjectSubtypeDto.class));
    assertNotNull(objectSubtypeDto);
    assertEquals(testObjectSubtype.getUuid(), objectSubtypeDto.getUuid());
    assertEquals("drawing", objectSubtypeDto.getAcSubtype());
    assertEquals(testObjectSubtype.getDcType(), objectSubtypeDto.getDcType());
  }
    
}
