package ca.gc.aafc.objectstore.api.validation;

import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute.ManagedAttributeType;

public class MetadataManagedAttributeValidator implements Validator {
 
    private ReloadableResourceBundleMessageSource messageSource;

    public MetadataManagedAttributeValidator(ReloadableResourceBundleMessageSource messageSource) {
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
        String[] acceptedValues = ma.getAcceptedValues();
        ManagedAttributeType maType = ma.getManagedAttributeType();
        
        if (acceptedValues == null || acceptedValues.length == 0) {
            if (maType == ManagedAttributeType.INTEGER) {
                Pattern pattern = Pattern.compile("\\d+");
                if (!pattern.matcher(assignedValue).matches()) {
                    String errorMessage = messageSource.getMessage("assignedValueType.invalid", new String[] {assignedValue}, LocaleContextHolder.getLocale());
                    errors.rejectValue("assignedValue", "assignedValueType.invalid", new String[] {assignedValue}, errorMessage);
                }   
            }
        } else {
            if (!Stream.of(acceptedValues).map(x -> x.toUpperCase()).anyMatch(assignedValue.toUpperCase()::equals)) {
                String errorMessage = messageSource.getMessage("assignedValue.invalid", new String[] {assignedValue}, LocaleContextHolder.getLocale());
                errors.rejectValue("assignedValue", "assignedValue.invalid", new String[] {assignedValue}, errorMessage);
            }
        }        

    }
}
