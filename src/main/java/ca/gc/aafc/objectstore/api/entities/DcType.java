package ca.gc.aafc.objectstore.api.entities;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public enum DcType {
  IMAGE("Image"),
  MOVING_IMAGE("Moving Image", "video"),
  SOUND("Sound"),
  TEXT("Text"),
  DATASET("Dataset"), // Data encoded in a defined structure
  UNDETERMINED("Undetermined");
  
  private static final Pattern ALPHA_VALUE_ONLY = Pattern.compile("[^a-zA-Z]");

  private final String value;
  private final String dcFormatType;

  DcType(String value) {
    this(value, value.toLowerCase());
  }
  
  /**
   * Main DcType constructor.
   * 
   * @param value
   * @param dcFormatType
   *          represent the first part of the media type. For text/csv the dcFormatType would be
   *          "text".
   */
  DcType(String value, String dcFormatType) {
    this.value = value;
    this.dcFormatType = dcFormatType;
  }

  public String getValue() {
    return value;
  }
  
  public String getDcFormatType() {
    return dcFormatType;
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
  
  /**
   * Get the {@link DcType} value associated with the provided dcFormat. The string is matched in
   * a case insensitive manner. dcFormat is expected to be in the form of media type (e.g.
   * text/csv).
   * 
   * @param value
   *          in the form of media type (e.g. text/csv)
   * @return the {@link DcType} wrapped in an {@link Optional} or {@link Optional#empty()} if
   *         there is no match.
   */
  public static Optional<DcType> fromDcFormat(String dcFormat) {
    if (dcFormat == null) {
      return Optional.empty();
    }
    String dcFormatType = StringUtils.substringBefore(dcFormat, "/");

    for (DcType currType : values()) {
      if (currType.getDcFormatType().equalsIgnoreCase(dcFormatType)) {
        return Optional.of(currType);
      }
    }
    return Optional.empty();
  }
}
