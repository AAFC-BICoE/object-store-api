package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import lombok.NonNull;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class ObjectUploadService extends DinaService<ObjectUpload> {

  private final MessageSource messageSource;

  public ObjectUploadService(@NonNull BaseDAO baseDAO, MessageSource messageSource) {
    super(baseDAO);
    this.messageSource = messageSource;
  }

  @Override
  protected void preCreate(ObjectUpload entity) {
    throw new AccessDeniedException(getMessage("error.objectUpload.create_unsupported"));
  }

  @Override
  protected void preDelete(ObjectUpload entity) {
    throw new AccessDeniedException(getMessage("error.objectUpload.delete_unsupported"));
  }

  @Override
  protected void preUpdate(ObjectUpload entity) {
    throw new AccessDeniedException(getMessage("error.objectUpload.update_unsupported"));
  }

  private String getMessage(String key) {
    return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
  }

}
