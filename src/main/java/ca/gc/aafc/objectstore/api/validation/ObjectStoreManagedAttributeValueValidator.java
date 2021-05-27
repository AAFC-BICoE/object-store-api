package ca.gc.aafc.objectstore.api.validation;

import ca.gc.aafc.dina.validation.ManagedAttributeValueValidator;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreManagedAttribute;
import ca.gc.aafc.objectstore.api.service.ObjectStoreManagedAttributeService;
import lombok.NonNull;
import org.springframework.context.MessageSource;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;

import javax.inject.Named;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ObjectStoreManagedAttributeValueValidator extends ManagedAttributeValueValidator<ObjectStoreManagedAttribute> {

  public ObjectStoreManagedAttributeValueValidator(
    @NonNull ObjectStoreManagedAttributeService osmas,
    @Named("validationMessageSource") MessageSource messageSource
  ) {
    super(messageSource, osmas);
  }

  public void validateManagedAttributes(
    @NotNull Map<String, String> managedAttributeValues,
    @NonNull String objectName
  ) {
    BeanPropertyBindingResult errors = new BeanPropertyBindingResult(managedAttributeValues, objectName);
    this.validate(managedAttributeValues, errors);
    if (errors.hasErrors()) {
      throw new ValidationException(String.join(
        ". ",
        errors.getAllErrors()
          .stream()
          .map(DefaultMessageSourceResolvable::getCode)
          .collect(Collectors.toSet())));
    }
  }

}
