package ca.gc.aafc.objectstore.api.exceptionmapping;

import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ManagedAttributeChildConflictException extends RuntimeException {

  public static final int HTTP_CODE = 409;
  public final ManagedAttribute managedAttribute;
  public final List<MetadataManagedAttribute> children;

}
