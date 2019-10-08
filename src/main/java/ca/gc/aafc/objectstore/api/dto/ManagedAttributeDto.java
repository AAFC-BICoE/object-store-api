package ca.gc.aafc.objectstore.api.dto;

import ca.gc.aafc.objectstore.api.entities.ManagedAttribute.ManagedAttributeType;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@SuppressFBWarnings({ "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
public class ManagedAttributeDto {
  
  private String name;
  private ManagedAttributeType managedAttributeType;
  private String[] acceptedValues;
  
}
