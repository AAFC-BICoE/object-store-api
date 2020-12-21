package ca.gc.aafc.objectstore.api.service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import javax.inject.Inject;

import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import ca.gc.aafc.objectstore.api.MediaTypeToDcTypeConfiguration;
import ca.gc.aafc.objectstore.api.DefaultValueConfiguration;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import lombok.NonNull;

/**
 * Service that contains logic around setting default values for various fields on {@link ObjectStoreMetadata}.
 */
@Service
public class ObjectStoreMetadataDefaultValueSetterService {

  private final Map<String, String> defaultMetaValues;
  private final Set<Entry<DcType, LinkedList<Pattern>>> dcFormatToDcType;

  private static final String KEY_licenceUrl = "licenceUrl";
  private static final String KEY_copyright = "copyright";
  private static final String KEY_copyrightOwner = "copyrightOwner";
  private static final String KEY_usageTerms = "usageTerms";

  @Inject
  public ObjectStoreMetadataDefaultValueSetterService(
    DefaultValueConfiguration config,
    MediaTypeToDcTypeConfiguration mediaTypeToDcTypeConfiguration
  ) {
    this.defaultMetaValues = initDefaultMeta(config);
    this.dcFormatToDcType = mediaTypeToDcTypeConfiguration.getToDcType().entrySet();
  }

  private Map<String, String> initDefaultMeta(DefaultValueConfiguration config) {
    HashMap<String, String> map = new HashMap<>();
    if (config != null && CollectionUtils.isNotEmpty(config.getValues())) {
      config.getValues().stream()
        .filter(defaultValue -> StringUtils.equalsIgnoreCase(defaultValue.getType(), ObjectStoreMetadataDto.TYPENAME))
        .forEach(defaultValue -> map.putIfAbsent(defaultValue.getAttribute(), defaultValue.getValue()));
    }
    return map;
  }

  /**
   * Assigns default values to a specific {@link ObjectStoreMetadata} instance.
   * Defaults values are only set if the current value is null or blank.
   *
   * @param objectMetadata meta data to set
   * @return the meta data
   */
  public ObjectStoreMetadata assignDefaultValues(@NonNull ObjectStoreMetadata objectMetadata) {
    if (objectMetadata.getDcType() == null) {
      objectMetadata.setDcType(dcTypeFromDcFormat(objectMetadata.getDcFormat()));
    }

    if (StringUtils.isBlank(objectMetadata.getXmpRightsWebStatement())) {
      objectMetadata.setXmpRightsWebStatement(defaultMetaValues.get(KEY_licenceUrl));
    }

    if (StringUtils.isBlank(objectMetadata.getDcRights())) {
      objectMetadata.setDcRights(defaultMetaValues.get(KEY_copyright));
    }

    if (StringUtils.isBlank(objectMetadata.getXmpRightsOwner())) {
      objectMetadata.setXmpRightsOwner(defaultMetaValues.get(KEY_copyrightOwner));
    }

    if (StringUtils.isBlank(objectMetadata.getXmpRightsUsageTerms())) {
      objectMetadata.setXmpRightsUsageTerms(defaultMetaValues.get(KEY_usageTerms));
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
