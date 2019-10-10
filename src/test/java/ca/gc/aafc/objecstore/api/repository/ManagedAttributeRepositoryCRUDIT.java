package ca.gc.aafc.objecstore.api.repository;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.Serializable;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;

import ca.gc.aafc.objectstore.api.testsupport.DBBackedIntegrationTest;
import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.dto.ManagedAttributeDto;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.respository.ManagedAttributeResourceRepository;
import ca.gc.aafc.objectstore.api.respository.ObjectStoreResourceRepository;
import ca.gc.aafc.objectstore.api.testsupport.factories.ManagedAttributeFactory;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;

public class ManagedAttributeRepositoryCRUDIT extends BaseRepositoryTest {
  
  @Inject
  private ManagedAttributeResourceRepository managedResourceRepository;
  
  private ManagedAttribute testManagedAttribute;
  
  private ManagedAttribute createTestManagedAttribute() {
    testManagedAttribute = ManagedAttributeFactory.newManagedAttribute().build();
    testManagedAttribute.setAcceptedValues(new String[] {"dosal"});
    persist(testManagedAttribute);
    return testManagedAttribute;
  }
  
  @BeforeEach
  public void setup() { 
    createTestManagedAttribute();    
  }  

  @Test
  public void findManagedAttribute_whenNoFieldsAreSelected_manageAttributeReturnedWithAllFields() {
    ManagedAttributeDto managedAttributeDto = managedResourceRepository.findOne(
        testManagedAttribute.getUuid(),
        new QuerySpec(ManagedAttributeDto.class)
    );  
    assertNotNull(managedAttributeDto);
    assertEquals(testManagedAttribute.getUuid(), managedAttributeDto.getUuid());
    System.out.println("managedAttributeDto.getAcceptedValues() " +managedAttributeDto.getAcceptedValues());
    assertArrayEquals(testManagedAttribute.getAcceptedValues(), 
        (String[])(managedAttributeDto.getAcceptedValues().toArray(new String[0])));
    assertEquals(testManagedAttribute.getManagedAttributeType(), 
        managedAttributeDto.getManagedAttributeType());
    assertEquals(testManagedAttribute.getName(), managedAttributeDto.getName());    
    
  }
    
}
