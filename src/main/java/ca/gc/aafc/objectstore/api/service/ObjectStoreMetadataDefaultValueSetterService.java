package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.objectstore.api.DefaultValueConfiguration;
import ca.gc.aafc.objectstore.api.MediaTypeToDcTypeConfiguration;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Service that contains logic around setting default values for various fields on {@link ObjectStoreMetadata}.
 */
@Service
public class ObjectStoreMetadataDefaultValueSetterService {

  private final Map<String, String> defaultMetaValues;
  private final Set<Entry<DcType, LinkedList<Pattern>>> dcFormatToDcType;

  @Inject
  public ObjectStoreMetadataDefaultValueSetterService(
    DefaultValueConfiguration config,
    MediaTypeToDcTypeConfiguration mediaTypeToDcTypeConfiguration
  ) {
    this.defaultMetaValues = initDefaultMeta(config);
    this.dcFormatToDcType = mediaTypeToDcTypeConfiguration.getToDcType().entrySet();
  }

  private static Map<String, String> initDefaultMeta(DefaultValueConfiguration config) {
    Map<String, String> map = new HashMap<>();
    if (config != null && CollectionUtils.isNotEmpty(config.getValues())) {
      config.getValues().stream()
        .filter(v -> StringUtils.equalsIgnoreCase(v.getType(), ObjectStoreMetadataDto.TYPENAME))
        .forEach(value -> map.putIfAbsent(value.getAttribute(), value.getValue()));
    }
    return Map.copyOf(map);
  }

  /**
   * Assigns default values to a specific {@link ObjectStoreMetadata} instance.
   * Defaults values are only set if the current value is null or blank.
   *
   * @param objectMetadata meta data to set
   * @return the meta data
   */
  @SneakyThrows
  public ObjectStoreMetadata assignDefaultValues(@NonNull ObjectStoreMetadata objectMetadata) {
    if (objectMetadata.getDcType() == null) {
      objectMetadata.setDcType(dcTypeFromDcFormat(objectMetadata.getDcFormat()));
    }

    if (objectMetadata.getPubliclyReleasable() == null) {
      Pair<Boolean, String> isPubliclyReleasable = isPubliclyReleasable(objectMetadata.getDcType());
      if (isPubliclyReleasable.getLeft()) {
        objectMetadata.setPubliclyReleasable(true);
      }
      else {
        objectMetadata.setPubliclyReleasable(false);
        objectMetadata.setNotPubliclyReleasableReason(isPubliclyReleasable.getRight());
      }
    }

    for (Entry<String, String> entry : defaultMetaValues.entrySet()) {
      if (StringUtils.isBlank(BeanUtils.getProperty(objectMetadata, entry.getKey()))) {
        BeanUtils.setProperty(objectMetadata, entry.getKey(), entry.getValue());
      }
    }
    return objectMetadata;
  }
  
  public DcType dcTypeFromDcFormat(String dcFormat) {
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

  /**
   * Get the default value for "Publicly Releasable" based on DcType with the reason if isPubliclyReleasable is false.
   * @param dcType dcType of the object
   * @return is the object "Publicly Releasable" by default based on the current business rule with the reason if it's not
   */
  public static Pair<Boolean, String> isPubliclyReleasable(@NonNull DcType dcType) {
    if (dcType == DcType.IMAGE) {
      return Pair.of(Boolean.TRUE, null);
    }
    return Pair.of(Boolean.FALSE, "default based on Type : " + dcType.getValue());
  }

}
