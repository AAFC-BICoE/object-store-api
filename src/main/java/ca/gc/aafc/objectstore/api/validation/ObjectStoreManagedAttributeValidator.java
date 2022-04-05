package ca.gc.aafc.objectstore.api.validation;

import ca.gc.aafc.objectstore.api.entities.ObjectStoreManagedAttribute;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ObjectStoreManagedAttributeValidator implements Validator {

  private final MessageSource messageSource;

  public ObjectStoreManagedAttributeValidator(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return ObjectStoreManagedAttribute.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    ObjectStoreManagedAttribute ma = (ObjectStoreManagedAttribute) target;

    if (ma.getMultilingualDescription().hasBlankDescription()) {
      String errorMessage = messageSource.getMessage("description.isEmpty", null,
        LocaleContextHolder.getLocale());
      errors.rejectValue("multilingualDescription", "description.isEmpty", null, errorMessage);
    }
  }
}
