package ca.gc.aafc.objectstore.api.validation;

import ca.gc.aafc.objectstore.api.entities.Derivative;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class DerivativeValidator implements Validator {

  private final MessageSource messageSource;

  @Override
  public boolean supports(@NonNull Class<?> aClass) {
    return Derivative.class.isAssignableFrom(aClass);
  }

  @Override
  public void validate(@NonNull Object o, @NonNull Errors errors) {
    if (!supports(o.getClass())) {
      throw new IllegalArgumentException(
        "this validator only supports the " + Derivative.class.getSimpleName() + " class.");
    }

    Derivative entity = (Derivative) o;

  }
}
