package ca.gc.aafc.objectstore.api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import javax.inject.Inject;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.gc.aafc.objectstore.api.DinaAuthenticatedUserConfig;
import ca.gc.aafc.objectstore.api.dto.ObjectSubtypeDto;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectSubtypeFactory;
import io.crnk.core.queryspec.QuerySpec;

public class ObjectSubtypeRepositoryCRUDIT extends BaseIntegrationTest {
  
  @Inject
  private ObjectSubtypeResourceRepository objectSubtypeRepository;
  
  private ObjectSubtype testObjectSubtype;

  private final static String DINA_USER_NAME = DinaAuthenticatedUserConfig.USER_NAME;
  
  private ObjectSubtype createTestAcSubtype() {
    testObjectSubtype = ObjectSubtypeFactory.newObjectSubtype()
        .build();

    return objectSubtypeService.create(testObjectSubtype);
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
    assertEquals(testObjectSubtype.getAcSubtype(), objectSubtypeDto.getAcSubtype());
    assertEquals(testObjectSubtype.getDcType(), objectSubtypeDto.getDcType());
    assertEquals(testObjectSubtype.getCreatedBy(), objectSubtypeDto.getCreatedBy());
  }
  
  @Test
  public void create_WithAuthenticatedUser_SetsCreatedBy() {
    ObjectSubtypeDto os = new ObjectSubtypeDto();
    os.setUuid(UUID.randomUUID());
    os.setAcSubtype("test subtype".toUpperCase());
    os.setDcType(DcType.IMAGE);
    ObjectSubtypeDto result = objectSubtypeRepository.findOne(objectSubtypeRepository.create(os).getUuid(),
        new QuerySpec(ObjectSubtypeDto.class));
    assertEquals(DINA_USER_NAME, result.getCreatedBy());
  }
    
}
