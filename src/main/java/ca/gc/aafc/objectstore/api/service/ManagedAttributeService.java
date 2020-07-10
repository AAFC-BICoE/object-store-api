package ca.gc.aafc.objectstore.api.service;

import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import lombok.NonNull;

@Service
public class ManagedAttributeService extends DinaService<ManagedAttribute> {

  public ManagedAttributeService(@NonNull BaseDAO baseDAO) {
    super(baseDAO);
  }

  @Override
  protected ManagedAttribute preCreate(ManagedAttribute entity) {
    return entity;
  }

  @Override
  protected void preDelete(ManagedAttribute entity) {
    // Do nothing
  }

  @Override
  protected ManagedAttribute preUpdate(ManagedAttribute entity) {
    return entity;
  }

}
