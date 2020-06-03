package ca.gc.aafc.objectstore.api.service;

import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import ca.gc.aafc.objectstore.api.MediaTypeToDcTypeConfiguration;
import ca.gc.aafc.objectstore.api.ObjectStoreConfiguration;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import lombok.NonNull;

/**
 * 
 * Service that contains logic around setting default values for various fields on {@link ObjectStoreMetadata}.
 *
 */
@Service
public class ObjectStoreMetadataDefaultValueSetterService {
  
  private final ObjectStoreConfiguration config;
  private final Set<Entry<DcType, LinkedList<Pattern>>> dcFormatToDcType;
  
  @Inject
  public ObjectStoreMetadataDefaultValueSetterService(ObjectStoreConfiguration config,
      MediaTypeToDcTypeConfiguration mediaTypeToDcTypeConfiguration) {
    this.config = config;
    dcFormatToDcType = mediaTypeToDcTypeConfiguration.getToDcType().entrySet();
  }
  
  /**
   * Assigns default values to a specific {@link ObjectStoreMetadata} instance.
   * Defaults values are only set if the current value is null or blank.
   * 
   * @param objectMetadata
   * @return
   */
  public ObjectStoreMetadataDto assignDefaultValues(@NonNull ObjectStoreMetadataDto objectMetadata) {
    if (objectMetadata.getDcType() == null) {
      objectMetadata.setDcType(dcTypeFromDcFormat(objectMetadata.getDcFormat()));
    }

    if (StringUtils.isBlank(objectMetadata.getXmpRightsWebStatement())) {
      objectMetadata.setXmpRightsWebStatement(config.getDefaultLicenceURL());
    }

    if (StringUtils.isBlank(objectMetadata.getDcRights())) {
      objectMetadata.setDcRights(config.getDefaultCopyright());
    }

    if (StringUtils.isBlank(objectMetadata.getXmpRightsOwner())) {
      objectMetadata.setXmpRightsOwner(config.getDefaultCopyrightOwner());
    }
    
    if (StringUtils.isBlank(objectMetadata.getXmpRightsUsageTerms())) {
      objectMetadata.setXmpRightsUsageTerms(config.getDefaultUsageTerms());
    }    

    return objectMetadata;
  }
  
  private DcType dcTypeFromDcFormat(String dcFormat) {
    if (StringUtils.isBlank(dcFormat)) {
      return DcType.UNDETERMINED;
    }
    for (Entry<DcType, LinkedList<Pattern>> entry : dcFormatToDcType) {
      for (Pattern pattern : entry.getValue()) {
        if (pattern.matcher(dcFormat).matches()) {
          return entry.getKey();
        }
      }
    }
    return DcType.UNDETERMINED;
  }

}
