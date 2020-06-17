package ca.gc.aafc.objectstore.api.validation;

import java.util.Arrays;

import javax.annotation.Resource;
import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;

public class MetadataManagedAttributeValidator implements Validator {

    @Inject
    private MessageSource messageSource;

    @Autowired
    public MetadataManagedAttributeValidator(MessageSource messageSource){
        this.messageSource = messageSource;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return MetadataManagedAttribute.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        //Is this required? Already has an @NotNull annotation
        ValidationUtils.rejectIfEmpty(errors, "managedAttribute", "managedAttribute.empty", errorArgs);
        
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "assignedValue", errorCode, errorArgs);
        
        MetadataManagedAttribute mma = (MetadataManagedAttribute) target;
        
        String[] acceptedValues = mma.getManagedAttribute().getAcceptedValues();
        // check that assignedValue is contained in acceptedValues
        //could combine if and else if decide to keep the same error message
        if (acceptedValues.equals(null)) {
            errors.rejectValue("assignedValue", "assignedValue.invalid"/*, arg2*/);
        } else {
            if (!Arrays.stream(acceptedValues).anyMatch(mma.getAssignedValue()::equals)){
                errors.rejectValue("assignedValue", "assignedValue.invalid"/*, arg2*/);
            }
        }
        // check that assignedValue type is of type contained in ManagedAttributeType

    }
}
