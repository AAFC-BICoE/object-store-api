package ca.gc.aafc.objectstore.api.validation;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;

@Component
public class ManagedAttributeValidator implements Validator {
  
  private final MessageSource messageSource;

  public ManagedAttributeValidator(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return MetadataManagedAttribute.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    ManagedAttribute ma = (ManagedAttribute) target;
    if (ma.getDescription().isEmpty()) {
      String errorMessage = messageSource.getMessage("description.isEmpty", null,
          LocaleContextHolder.getLocale());
      errors.rejectValue("description", "description.isEmpty", null, errorMessage);
    }
    
  }

  
}