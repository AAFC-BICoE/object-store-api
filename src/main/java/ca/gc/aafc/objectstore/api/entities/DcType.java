package ca.gc.aafc.objectstore.api.entities;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public enum DcType {
  IMAGE("Image"),
  MOVING_IMAGE("Moving Image"),
  SOUND("Sound"),
  TEXT("Text"),
  DATASET("Dataset"), // Data encoded in a defined structure
  UNDETERMINED("Undetermined");
  
  private static final Pattern ALPHA_VALUE_ONLY = Pattern.compile("[^a-zA-Z]");

  private final String value;

  DcType(String value) {
    this.value = value;
  }
  
  public String getValue() {
    return value;
  }

  /**
   * Get the {@link DcType} value from the provided string. The string is matched in a case
   * insensitive manner.
   * 
   * @param value
   * @return the {@link DcType} wrapped in an {@link Optional} or {@link Optional#empty()} is no
   *         there is {@link DcType} match.
   */
  public static Optional<DcType> fromValue(String value) {
    if(StringUtils.isBlank(value)) {
      return Optional.empty();
    }
    
    String alphaOnlyValue = ALPHA_VALUE_ONLY.matcher(value).replaceAll("");
    for (DcType currType : values()) {
      if (alphaOnlyValue.equalsIgnoreCase(ALPHA_VALUE_ONLY.matcher(currType.getValue()).replaceAll(""))) {
        return Optional.of(currType);
      }
    }
    return Optional.empty();
  }
  
}
