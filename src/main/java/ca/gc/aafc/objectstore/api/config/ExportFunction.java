package ca.gc.aafc.objectstore.api.config;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import lombok.Builder;

import org.springframework.http.MediaType;

import static ca.gc.aafc.objectstore.api.config.MediaTypeConfiguration.CANON_CR2_MEDIA_TYPE;
import static ca.gc.aafc.objectstore.api.config.MediaTypeConfiguration.NIKON_NEF_MEDIA_TYPE;

/**
 * Pre-defined functions that can be used on exports of objects.
 * @param functionDef
 * @param params
 */
@Builder
public record ExportFunction(FunctionDef functionDef, Map<String, String> params) {

  public static final String IMG_RESIZE_PARAM_FACTOR = "factor";

  public static final String MAGICK_PARAM_QUALITY = "quality";
  public static final String MAGICK_PARAM_ROTATION = "rotation";
  public static final String MAGICK_PARAM_TARGET_MEDIA_TYPE = "targetMediaType";

  public enum FunctionDef {
    IMG_RESIZE(Set.of(MediaType.IMAGE_JPEG_VALUE), ExportFunction::imageResizeParamValidator,
      "resized", false),
    MAGICK(Set.of(MediaType.IMAGE_JPEG_VALUE, CANON_CR2_MEDIA_TYPE.toString(),
      NIKON_NEF_MEDIA_TYPE.toString()), ExportFunction::magickParamValidator, "converted", true);

    private final Set<String> supportedMediaType;
    private final Predicate<Map<String, String>> paramsValidator;
    private final String suffix;
    private final boolean isChangingMediaType;

    FunctionDef(Set<String> supportedMediaType, Predicate<Map<String, String>> paramsValidator, String suffix,
                boolean isChangingMediaType) {
      this.supportedMediaType = supportedMediaType;
      this.paramsValidator = paramsValidator;
      this.suffix = suffix;
      this.isChangingMediaType = isChangingMediaType;
    }

    public boolean isMediaTypeSupported(String mediaType) {
      return supportedMediaType.contains(mediaType);
    }

    public boolean areParamsValid(Map<String, String> params) {
      return paramsValidator.test(params);
    }

    public boolean isChangingMediaType() {
      return isChangingMediaType;
    }

    public String getSuffix() {
      return suffix;
    }
  }

  /**
   * Checks if the function can be applied on the provided media type.
   * @param mediaType
   * @return
   */
  public boolean isMediaTypeSupported(String mediaType) {
    return functionDef.isMediaTypeSupported(mediaType);
  }

  public boolean areParamsValid() {
    return functionDef.areParamsValid(params);
  }

  public Optional<String> getGeneratedMediaType() {
    if (functionDef() == FunctionDef.MAGICK) {
      return Optional.of(params.get(MAGICK_PARAM_TARGET_MEDIA_TYPE));
    }
    return Optional.empty();
  }

  private static boolean imageResizeParamValidator(Map<String, String> params) {
    if (params == null || params.size() != 1 || !params.containsKey(IMG_RESIZE_PARAM_FACTOR)) {
      return false;
    }

    try {
      float value = Float.parseFloat(params.get(IMG_RESIZE_PARAM_FACTOR));
      return value > 0 && value < 1;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  private static boolean magickParamValidator(Map<String, String> params) {
    if (params == null || params.isEmpty() || !params.containsKey(MAGICK_PARAM_TARGET_MEDIA_TYPE)) {
      return false;
    }

    var mediaType =
      org.apache.tika.mime.MediaType.parse(params.get(MAGICK_PARAM_TARGET_MEDIA_TYPE));
    if (MediaTypeConfiguration.isSupported(mediaType)) {

      // optional params
      try {
        if (params.containsKey(MAGICK_PARAM_QUALITY)) {
          Integer.parseInt(params.get(MAGICK_PARAM_QUALITY));
        }
        if (params.containsKey(MAGICK_PARAM_ROTATION)) {
          Integer.parseInt(params.get(MAGICK_PARAM_ROTATION));
        }
      } catch (NumberFormatException e) {
        return false;
      }
      return true;
    }
    return false;
  }

}
