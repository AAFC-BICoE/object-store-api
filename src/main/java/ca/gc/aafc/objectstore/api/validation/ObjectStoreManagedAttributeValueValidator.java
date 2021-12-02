package ca.gc.aafc.objectstore.api.validation;

import javax.inject.Named;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import ca.gc.aafc.dina.service.ManagedAttributeService;
import ca.gc.aafc.dina.validation.ManagedAttributeValueValidator;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreManagedAttribute;
import lombok.NonNull;

@Component
public class ObjectStoreManagedAttributeValueValidator extends ManagedAttributeValueValidator<ObjectStoreManagedAttribute> {

  public ObjectStoreManagedAttributeValueValidator(
    @Named("validationMessageSource") MessageSource baseMessageSource, // from dina-base
    @NonNull ManagedAttributeService<ObjectStoreManagedAttribute> dinaService
  ) {
    super(baseMessageSource, dinaService);
  }
  
}
