package ca.gc.aafc.objectstore.api.exceptionmapping;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ManagedAttributeChildConflictException extends RuntimeException {

  public static final String HTTP_CODE = "409";
  public static final String ERROR_TITLE = "Conflict";
  public final String managedAttributeId;
  public final List<String> childrenIds;

  @Override
  public String getMessage() {
    return "Managed attribute with identifier: " + managedAttributeId
      + ", is currently in use by metadata with the following identifiers: " +
      String.join(" , ", childrenIds);
  }
}
