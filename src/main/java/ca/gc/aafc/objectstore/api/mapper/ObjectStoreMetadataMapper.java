package ca.gc.aafc.objectstore.api.mapper;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata.DcType;

@Mapper
public interface ObjectStoreMetadataMapper {
  
  @Mappings({
    @Mapping(target="dcType", source="entity.dcType", qualifiedByName = "dcTypeConverterEntityToDto"),
    @Mapping(target="acDigitizationDate", source="entity.acDigitizationDate", qualifiedByName = "offsetDateTimeConverterEntityToDto",
    dateFormat = "dd-MM-yyyy HH:mm:ss"),
    @Mapping(target="xmpMetadataDdate", source="entity.xmpMetadataDdate", qualifiedByName = "offsetDateTimeConverterEntityToDto",
    dateFormat = "dd-MM-yyyy HH:mm:ss")  
  })
  
  ObjectStoreMetadataDto ObjectStoreMetadataToObjectStoreMetadataDto(ObjectStoreMetadata entity);
  
  @Mappings({
    @Mapping(target = "id", ignore = true),
    @Mapping(target="dcType", source="dto.dcType", qualifiedByName = "dcTypeConverterDtoToEntity"),
    @Mapping(target="acDigitizationDate", source="dto.acDigitizationDate",qualifiedByName = "offsetDateTimeConverterDtoToEntity",
    dateFormat = "dd-MM-yyyy HH:mm:ss"),
    @Mapping(target="xmpMetadataDdate", source="dto.xmpMetadataDdate", qualifiedByName = "offsetDateTimeConverterDtoToEntity",
    dateFormat = "dd-MM-yyyy HH:mm:ss")     
  })
  
  ObjectStoreMetadata ObjectStoreMetadataDtotoObjectStoreMetadata(ObjectStoreMetadataDto dto);
  
  @Named("dcTypeConverterEntityToDto")
  public static String dcTypeConverterEntityToDto(DcType dcType) {
    return dcType.getValue();
    
  }
  
  @Named("offsetDateTimeConverterEntityToDto")
  public static String offsetDateTimeConverterEntityToDto(OffsetDateTime offsetDateTime) {
    System.out.println("offsetDateTimeConverterEntityToDto acDigitizationDate "+ offsetDateTime.toString());
    return offsetDateTime.toString();    
  }
  
  @Named("dcTypeConverterDtoToEntity")
  public static DcType dcTypeConverterDtoToEntity(String dcType) {
    DcType myDcType = null;
    for(DcType a: DcType.values()) {
      if(dcType.equals(a.getValue())) {
        myDcType = a;
        break;
      }
    }
    return myDcType;
  }
  
  @Named("offsetDateTimeConverterDtoToEntity")
  public static OffsetDateTime offsetDateTimeConverterDtoToEntity(String offsetDateTimeString) {
    System.out.println("offsetDateTimeConverterEntityToDto acDigitizationDate "+ offsetDateTimeString);
    OffsetDateTime offsetDateTime = OffsetDateTime.parse(offsetDateTimeString,
        DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    return offsetDateTime;    
  }  
  
}
