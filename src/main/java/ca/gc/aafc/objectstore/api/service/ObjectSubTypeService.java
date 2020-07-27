package ca.gc.aafc.objectstore.api.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import lombok.NonNull;

@Service
public class ObjectSubTypeService extends DinaService<ObjectSubtype> {

  private static final String APP_MANAGED = "This sub type is app managed and cannot be updated/deleted";

  public ObjectSubTypeService(@NonNull BaseDAO baseDAO) {
    super(baseDAO);
  }

  @Override
  protected void preCreate(ObjectSubtype entity) {
    if (entity.isAppManaged()) {
      throw new AccessDeniedException("Cannot create a subtype as app managed, set app managed to false");
    }
  }

  @Override
  protected void preDelete(ObjectSubtype entity) {
    if (entity.isAppManaged()) {
      throw new AccessDeniedException(APP_MANAGED);
    }
  }

  @Override
  protected void preUpdate(ObjectSubtype entity) {
    if (entity.isAppManaged()) {
      throw new AccessDeniedException(APP_MANAGED);
    }
  }

}
