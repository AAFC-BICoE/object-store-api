package ca.gc.aafc.objectstore.api.service;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DefaultDinaService;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import lombok.NonNull;

@Service
public class ObjectSubTypeService extends DefaultDinaService<ObjectSubtype> {

  private final MessageSource messageSource;

  public ObjectSubTypeService(@NonNull BaseDAO baseDAO, MessageSource messageSource) {
    super(baseDAO);
    this.messageSource = messageSource;
  }

  private String getMessage(String key) {
    return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
  }

}
