package ca.gc.aafc.objectstore.api.validation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute.ManagedAttributeType;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;

@Component
public class MetadataManagedAttributeValidator implements Validator {

  private final MessageSource messageSource;
  private static final Pattern INTEGER_PATTERN = Pattern.compile("\\d+");

  public MetadataManagedAttributeValidator(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return MetadataManagedAttribute.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    MetadataManagedAttribute mma = (MetadataManagedAttribute) target;
    String assignedValue = mma.getAssignedValue();
    ManagedAttribute ma = mma.getManagedAttribute();

    List<String> acceptedValues = ma.getAcceptedValues() == null ? Collections.emptyList()
        : Arrays.stream(ma.getAcceptedValues()).map(String::toUpperCase).collect(Collectors.toList());

    ManagedAttributeType maType = ma.getManagedAttributeType();
    boolean assignedValueIsValid = true;

    if (acceptedValues.isEmpty()) {
      if (maType == ManagedAttributeType.INTEGER && !INTEGER_PATTERN.matcher(assignedValue).matches()) {
        assignedValueIsValid = false;
      }
    } else {
      if (!acceptedValues.contains(assignedValue.toUpperCase())) {
        assignedValueIsValid = false;
      }
    }
    if (!assignedValueIsValid) {
      String errorMessage = messageSource.getMessage("assignedValue.invalid", new String[] { assignedValue },
          LocaleContextHolder.getLocale());
      errors.rejectValue("assignedValue", "assignedValue.invalid", new String[] { assignedValue }, errorMessage);
    }
  }

}
