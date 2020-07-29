package ca.gc.aafc.objectstore.api.service;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import lombok.NonNull;

@Service
public class ObjectSubTypeService extends DinaService<ObjectSubtype> {

  private final MessageSource messageSource;

  public ObjectSubTypeService(@NonNull BaseDAO baseDAO, MessageSource messageSource) {
    super(baseDAO);
    this.messageSource = messageSource;
  }

  @Override
  protected void preCreate(ObjectSubtype entity) {
    if (entity.isAppManaged()) {
      throw new AccessDeniedException(getMessage("error.appManaged.create_unsupported"));
    }
  }

  @Override
  protected void preDelete(ObjectSubtype entity) {
    if (entity.isAppManaged()) {
      throw new AccessDeniedException(getMessage("error.appManaged.read_only"));
    }
  }

  @Override
  protected void preUpdate(ObjectSubtype entity) {
    if (entity.isAppManaged()) {
      throw new AccessDeniedException(getMessage("error.appManaged.update_unsupported"));
    }
  }

  private String getMessage(String key) {
    return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
  }

}
