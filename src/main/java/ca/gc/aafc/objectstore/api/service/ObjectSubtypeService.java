package ca.gc.aafc.objectstore.api.service;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DefaultDinaService;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import java.util.UUID;

import lombok.NonNull;
import org.springframework.validation.SmartValidator;

@Service
public class ObjectSubtypeService extends DefaultDinaService<ObjectSubtype> {

  private final MessageSource messageSource;

  public ObjectSubtypeService(
    @NonNull BaseDAO baseDAO,
    MessageSource messageSource,
    @NonNull SmartValidator smartValidator
  ) {
    super(baseDAO, smartValidator);
    this.messageSource = messageSource;
  }

  private String getMessage(String key) {
    return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
  }

  @Override
  protected void preCreate(ObjectSubtype entity) {
    entity.setUuid(UUID.randomUUID());
  }

}
