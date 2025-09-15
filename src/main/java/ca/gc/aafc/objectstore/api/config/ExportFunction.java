package ca.gc.aafc.objectstore.api.config;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import lombok.Builder;

import org.springframework.http.MediaType;

/**
 * Pre-defined functions that can be used on exports of objects.
 * @param functionDef
 * @param params
 */
@Builder
public record ExportFunction(FunctionDef functionDef, Map<String, String> params) {

  public static final String IMG_RESIZE_PARAM_FACTOR = "factor";

  public enum FunctionDef {
    IMG_RESIZE(Set.of(MediaType.IMAGE_JPEG_VALUE), ExportFunction::imageResizeParamValidator, "resized");

    private final Set<String> supportedMediaType;
    private final Predicate<Map<String, String>> paramsValidator;
    private final String suffix;

    FunctionDef(Set<String> supportedMediaType, Predicate<Map<String, String>> paramsValidator, String suffix) {
      this.supportedMediaType = supportedMediaType;
      this.paramsValidator = paramsValidator;
      this.suffix = suffix;
    }

    public boolean isMediaTypeSupported(String mediaType) {
      return supportedMediaType.contains(mediaType);
    }

    public boolean areParamsValid(Map<String, String> params) {
      return paramsValidator.test(params);
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

}
