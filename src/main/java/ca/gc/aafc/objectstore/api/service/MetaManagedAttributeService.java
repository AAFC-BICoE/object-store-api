package ca.gc.aafc.objectstore.api.service;

import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import lombok.NonNull;

@Service
public class MetaManagedAttributeService extends DinaService<MetadataManagedAttribute> {

  public MetaManagedAttributeService(@NonNull BaseDAO baseDAO) {
    super(baseDAO);
  }

  @Override
  protected MetadataManagedAttribute preCreate(MetadataManagedAttribute entity) {
    return entity;
  }

  @Override
  protected void preDelete(MetadataManagedAttribute entity) {
    // Do nothing
  }

  @Override
  protected MetadataManagedAttribute preUpdate(MetadataManagedAttribute entity) {
    return entity;
  }

}
