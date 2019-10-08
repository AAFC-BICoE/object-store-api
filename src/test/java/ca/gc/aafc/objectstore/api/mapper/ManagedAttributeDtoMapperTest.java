package ca.gc.aafc.objectstore.api.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import ca.gc.aafc.objectstore.api.dto.ManagedAttributeDto;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute.ManagedAttributeType;
import ca.gc.aafc.objectstore.api.testsupport.factories.ManagedAttributeFactory;

public class ManagedAttributeDtoMapperTest {

  private static final ManagedAttributeMapper DTO_MAPPER = Mappers
      .getMapper(ManagedAttributeMapper.class);

  @Test
  public void testGivenManagedAttribute_mapsToManagedAttributeDto() {
    
    String[] acceptedValues  = new String[] {"CataloguedObject"};
    
    // given
    ManagedAttribute ManagedAttribute = ManagedAttributeFactory.newManagedAttribute()
        .acceptedValues(acceptedValues).build();

    // when
    ManagedAttributeDto ManagedAttributeDto = DTO_MAPPER
        .managedAttributeToManagedAttributeDto(ManagedAttribute);

    // then
    assertEquals(ManagedAttributeDto.getName(),
        ManagedAttribute.getName());
    assertEquals(ManagedAttributeDto.getManagedAttributeType(), ManagedAttribute.getManagedAttributeType());
    int arrLen = ManagedAttributeDto.getAcceptedValues().length;
    assertEquals(arrLen, ManagedAttribute.getAcceptedValues().length);
    for(int i=0 ; i< arrLen; i++) {
      assertEquals(ManagedAttributeDto.getAcceptedValues()[i], ManagedAttribute.getAcceptedValues()[i]);      
    }
  }


  @Test
  public void testGivenManagedAttributeDto_mapsToManagedAttribute() {
    
    String[] acceptedValues  = new String[] {"dosal"};
    
    // given
    ManagedAttributeDto ManagedAttributeDto = new ManagedAttributeDto();
    ManagedAttributeDto.setName("specimen_view");
    ManagedAttributeDto.setManagedAttributeType(ManagedAttributeType.STRING);
    ManagedAttributeDto.setAcceptedValues(acceptedValues);

    // when
    ManagedAttribute ManagedAttribute = DTO_MAPPER
        .managedAttributeDtoToManagedAttribute(ManagedAttributeDto);

    // then
    assertEquals(ManagedAttribute.getName(),
        ManagedAttributeDto.getName());
    assertEquals(ManagedAttribute.getManagedAttributeType(), ManagedAttributeDto.getManagedAttributeType());
    int arrLen = ManagedAttribute.getAcceptedValues().length;
    assertEquals(arrLen, ManagedAttributeDto.getAcceptedValues().length);
    for(int i = 0 ; i< arrLen; i++) {
      assertEquals(ManagedAttribute.getAcceptedValues()[i], ManagedAttributeDto.getAcceptedValues()[i]);      
    }    
  }

}
