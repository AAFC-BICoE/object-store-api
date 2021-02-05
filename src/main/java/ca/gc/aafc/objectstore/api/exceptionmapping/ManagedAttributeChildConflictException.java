package ca.gc.aafc.objectstore.api.exceptionmapping;

import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ManagedAttributeChildConflictException extends RuntimeException {

  public static final String HTTP_CODE = "409";
  public static final String ERROR_TITLE = "Conflict";
  public final ManagedAttribute managedAttribute;
  public final List<MetadataManagedAttribute> children;

  @Override
  public String getMessage() {
    return "Managed attribute with identifier: " + managedAttribute.getUuid()
      + ", is currently in use by metadata with the following identifiers: " + children.stream()
      .map(metadataManagedAttribute -> metadataManagedAttribute.getUuid().toString())
      .collect(Collectors.joining(" , "));
  }
}
