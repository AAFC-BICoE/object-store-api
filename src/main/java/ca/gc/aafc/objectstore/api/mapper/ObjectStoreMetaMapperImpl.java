package ca.gc.aafc.objectstore.api.mapper;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata.DcType;

public class ObjectStoreMetaMapperImpl implements ObjectStoreMetadataMapper{

  @Override
  public ObjectStoreMetadataDto ObjectStoreMetadataToObjectStoreMetadataDto(
      ObjectStoreMetadata entity) {
    
    if(entity == null)
      return null;
    
    ObjectStoreMetadataDto objectStoreMetadataDto = new ObjectStoreMetadataDto();
    objectStoreMetadataDto.setDcFormat(entity.getDcFormat()==null? "":entity.getDcFormat());
    objectStoreMetadataDto.setDcType(entity.getDcType().getValue());
    objectStoreMetadataDto.setAcDigitizationDate(entity.getAcDigitizationDate().toString());
    objectStoreMetadataDto.setXmpMetadataDate(entity.getXmpMetadataDate().toString());
       
    objectStoreMetadataDto.setAcHashFunction(entity.getAcHashFunction()==null? "":entity.getAcHashFunction());
    objectStoreMetadataDto.setAcHashValue(entity.getAcHashValue()==null? "":entity.getAcHashValue());
    
    objectStoreMetadataDto.setUuid(entity.getUuid());
        
    return objectStoreMetadataDto;
  }

  @Override
  public ObjectStoreMetadata ObjectStoreMetadataDtotoObjectStoreMetadata(
      ObjectStoreMetadataDto dto) {
    if(dto == null)
      return null;
    DcType myDcType = null;
    for(DcType a: DcType.values()) {
      if(dto.getDcType().equals(a.getValue())) {
        myDcType = a;
        break;
      }
    }
    OffsetDateTime acDigitizationDate = OffsetDateTime.parse(dto.getAcDigitizationDate(),
        DateTimeFormatter.ISO_OFFSET_DATE_TIME);    
    
    OffsetDateTime xmpMetadataDate = OffsetDateTime.parse(dto.getXmpMetadataDate(),
        DateTimeFormatter.ISO_OFFSET_DATE_TIME);    
    
    ObjectStoreMetadata objectStoreMetadata = new ObjectStoreMetadata();
    objectStoreMetadata.setDcFormat(dto.getDcFormat());
    objectStoreMetadata.setDcType(myDcType);
    objectStoreMetadata.setAcDigitizationDate(acDigitizationDate);
    objectStoreMetadata.setXmpMetadataDate(xmpMetadataDate);
    objectStoreMetadata.setAcHashFunction(dto.getAcHashFunction());
    objectStoreMetadata.setAcHashValue(dto.getAcHashValue());
    objectStoreMetadata.setUuid(dto.getUuid());
    
    return objectStoreMetadata;
  }

}
