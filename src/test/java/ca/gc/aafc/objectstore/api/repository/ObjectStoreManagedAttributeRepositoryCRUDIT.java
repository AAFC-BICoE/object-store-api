package ca.gc.aafc.objectstore.api.repository;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import ca.gc.aafc.objectstore.api.DinaAuthenticatedUserConfig;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreManagedAttributeDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreManagedAttribute;
import ca.gc.aafc.dina.entity.ManagedAttribute.ManagedAttributeType;
import ca.gc.aafc.dina.i18n.MultilingualDescription;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreManagedAttributeFactory;
import io.crnk.core.queryspec.QuerySpec;

public class ObjectStoreManagedAttributeRepositoryCRUDIT extends BaseIntegrationTest {
  
  @Inject
  private ObjectStoreManagedAttributeResourceRepository managedResourceRepository;
  
  private ObjectStoreManagedAttribute testManagedAttribute;

  private final static String DINA_USER_NAME = DinaAuthenticatedUserConfig.USER_NAME;

  private ObjectStoreManagedAttribute createTestManagedAttribute() throws JsonProcessingException {
    testManagedAttribute = ObjectStoreManagedAttributeFactory.newManagedAttribute()
        .acceptedValues(new String[] { "dosal" })
        .multilingualDescription(MultilingualDescription.builder()
          .descriptions(List.of(
            MultilingualDescription.MultilingualPair.builder()
              .desc("attrEn")
              .lang("en")
              .build(), 
            MultilingualDescription.MultilingualPair.builder()
              .desc("attrFr")
              .lang("fr")
              .build())
            )
          .build())
        .build();

    return managedAttributeService.create(testManagedAttribute);
  }
  
  @BeforeEach
  public void setup() throws JsonProcessingException { 
    createTestManagedAttribute();    
  }  

  @Test
  public void findManagedAttribute_whenNoFieldsAreSelected_manageAttributeReturnedWithAllFields() {
    ObjectStoreManagedAttributeDto managedAttributeDto = managedResourceRepository
        .findOne(testManagedAttribute.getUuid(), new QuerySpec(ObjectStoreManagedAttributeDto.class));
    assertNotNull(managedAttributeDto);
    assertEquals(testManagedAttribute.getUuid(), managedAttributeDto.getUuid());
    assertArrayEquals(testManagedAttribute.getAcceptedValues(),
        managedAttributeDto.getAcceptedValues());
    assertEquals(testManagedAttribute.getManagedAttributeType(),
        managedAttributeDto.getManagedAttributeType());
    assertEquals(testManagedAttribute.getName(), managedAttributeDto.getName());
    assertEquals(testManagedAttribute.getMultilingualDescription().getDescriptions().get(0),
        managedAttributeDto.getMultilingualDescription().getDescriptions().get(0));
  }

  @Test
  public void create_WithAuthenticatedUser_SetsCreatedBy() {
    ObjectStoreManagedAttributeDto ma = new ObjectStoreManagedAttributeDto();
    ma.setUuid(UUID.randomUUID());
    ma.setName("name");
    ma.setManagedAttributeType(ManagedAttributeType.STRING);
    ma.setAcceptedValues(new String[] { "dosal" });
    ma.setMultilingualDescription(MultilingualDescription.builder()
      .descriptions(List.of(
        MultilingualDescription.MultilingualPair.builder()
          .desc("attrEn")
          .lang("en")
          .build(), 
        MultilingualDescription.MultilingualPair.builder()
          .desc("attrFr")
          .lang("fr")
          .build())
        )
    .build());

    ObjectStoreManagedAttributeDto result = managedResourceRepository.findOne(
      managedResourceRepository.create(ma).getUuid(),
      new QuerySpec(ObjectStoreManagedAttributeDto.class));
    assertEquals(DINA_USER_NAME, result.getCreatedBy());
  }
    
}
