package ca.gc.aafc.objectstore.api.service;

import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import io.crnk.core.exception.UnauthorizedException;
import lombok.NonNull;

@Service
public class ObjectSubTypeService extends DinaService<ObjectSubtype> {

  public ObjectSubTypeService(@NonNull BaseDAO baseDAO) {
    super(baseDAO);
  }

  @Override
  protected ObjectSubtype preCreate(ObjectSubtype entity) {
    return entity;
  }

  @Override
  protected void preDelete(ObjectSubtype entity) {
    if (entity.isAppManaged()) {
      throw new UnauthorizedException("This sub type is app managed and cannot be deleted");
    }
  }

  @Override
  protected ObjectSubtype preUpdate(ObjectSubtype entity) {
    return entity;
  }

}
