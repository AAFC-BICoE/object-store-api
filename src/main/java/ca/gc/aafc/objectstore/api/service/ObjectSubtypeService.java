package ca.gc.aafc.objectstore.api.service;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DefaultDinaService;
import ca.gc.aafc.dina.util.UUIDHelper;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;

import lombok.NonNull;
import org.springframework.validation.SmartValidator;

@Service
public class ObjectSubtypeService extends DefaultDinaService<ObjectSubtype> {

  public ObjectSubtypeService(
    @NonNull BaseDAO baseDAO,
    MessageSource messageSource,
    @NonNull SmartValidator smartValidator
  ) {
    super(baseDAO, smartValidator);
  }

  @Override
  protected void preCreate(ObjectSubtype entity) {
    entity.setUuid(UUIDHelper.generateUUIDv7());
  }

}
