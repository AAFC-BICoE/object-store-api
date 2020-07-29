package ca.gc.aafc.objectstore.api.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute.ManagedAttributeType;

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
    ArrayList<String> acceptedValues = getAcceptedValuesList(ma.getAcceptedValues());
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
  
  private ArrayList<String> getAcceptedValuesList(String[] acceptedValues) {
    if (acceptedValues != null) {
      ArrayList<String> result = new ArrayList<String>(Arrays.asList(acceptedValues));
      result.replaceAll(x -> x.toUpperCase());
      return result;
    } else {
      return new ArrayList<String>();
    }
  }
}
