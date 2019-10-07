package ca.gc.aafc.objectstore.api.mapper;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata.DcType;

public class ObjectStoreMetaMapperImpl implements ObjectStoreMetadataMapper{

  @Override
  public ObjectStoreMetadataDto objectStoreMetadataToObjectStoreMetadataDto(
      ObjectStoreMetadata entity) {
    
    if(entity == null) {
      return null;
    }

    ObjectStoreMetadataDto objectStoreMetadataDto = new ObjectStoreMetadataDto();
    objectStoreMetadataDto.setDcFormat(entity.getDcFormat()==null? "":entity.getDcFormat());
    objectStoreMetadataDto.setDcType(entity.getDcType().getValue());
    objectStoreMetadataDto.setAcDigitizationDate(entity.getAcDigitizationDate()==null? 
        "" : entity.getAcDigitizationDate().toString());
    objectStoreMetadataDto.setXmpMetadataDate(entity.getXmpMetadataDate() == null? 
        "" : entity.getXmpMetadataDate().toString());       
    objectStoreMetadataDto.setAcHashFunction(entity.getAcHashFunction()==null? "":entity.getAcHashFunction());
    objectStoreMetadataDto.setAcHashValue(entity.getAcHashValue()==null? "":entity.getAcHashValue());
    
    objectStoreMetadataDto.setUuid(entity.getUuid());
   
    return objectStoreMetadataDto;
  }

  @Override
  public ObjectStoreMetadata objectStoreMetadataDtotoObjectStoreMetadata(
      ObjectStoreMetadataDto dto) {
    if(dto == null) {
      return null;
    }
    
    List<DcType> elements = Stream.of(DcType.values()).filter(val->dto.getDcType().equals(val.getValue()))
    .collect(Collectors.toList());
    
    Optional<DcType> myDcType = elements.stream().findFirst();
    
    OffsetDateTime acDigitizationDate = OffsetDateTime.parse(dto.getAcDigitizationDate(),
        DateTimeFormatter.ISO_OFFSET_DATE_TIME);    
    
    OffsetDateTime xmpMetadataDate = OffsetDateTime.parse(dto.getXmpMetadataDate(),
        DateTimeFormatter.ISO_OFFSET_DATE_TIME);    
    
    ObjectStoreMetadata objectStoreMetadata = new ObjectStoreMetadata();
    objectStoreMetadata.setDcFormat(dto.getDcFormat());
    objectStoreMetadata.setDcType(myDcType.get());
    objectStoreMetadata.setAcDigitizationDate(acDigitizationDate);
    objectStoreMetadata.setXmpMetadataDate(xmpMetadataDate);
    objectStoreMetadata.setAcHashFunction(dto.getAcHashFunction());
    objectStoreMetadata.setAcHashValue(dto.getAcHashValue());
    objectStoreMetadata.setUuid(dto.getUuid());
    
    return objectStoreMetadata;
  }

}
