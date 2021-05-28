package ca.gc.aafc.objectstore.api.exceptionmapping;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * SHOULD BE REPLACED
 * This is a temporary class until a more generic approach is available where the message
 * could also be translated.
 */
@RequiredArgsConstructor
public class ObjectStoreManagedAttributeChildConflictException extends RuntimeException {

  public final String managedAttributeId;
  public final List<String> childrenIds;

  @Override
  public String getMessage() {
    return "Managed attribute with identifier: " + managedAttributeId
      + ", is currently in use by metadata with the following identifiers: " +
      String.join(" , ", childrenIds);
  }
}
