package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DefaultDinaService;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import lombok.NonNull;
import org.springframework.stereotype.Service;

@Service
public class MetaManagedAttributeService extends DefaultDinaService<MetadataManagedAttribute> {
  public MetaManagedAttributeService(@NonNull BaseDAO baseDAO) {
    super(baseDAO);
  }
}
