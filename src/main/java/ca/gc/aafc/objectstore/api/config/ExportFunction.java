package ca.gc.aafc.objectstore.api.config;

import java.util.List;
import lombok.Builder;

/**
 * Pre-defined functions that can be used on exports of objects.
 * @param functionName
 * @param params
 */
@Builder
public record ExportFunction(FunctionName functionName, List<String> params) {
  public enum FunctionName {IMG_RESIZE}
}
