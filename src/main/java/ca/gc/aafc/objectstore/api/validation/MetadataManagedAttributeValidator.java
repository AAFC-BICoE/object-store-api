package ca.gc.aafc.objectstore.api.validation;

import java.util.Arrays;
import java.util.stream.Stream;

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
    private final MessageSource messageSource;

    }

    @Override
    public boolean supports(Class<?> clazz) {
        return MetadataManagedAttribute.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        MetadataManagedAttribute mma = (MetadataManagedAttribute) target;
        
        String[] acceptedValues = mma.getManagedAttribute().getAcceptedValues();
        
        if (acceptedValues.equals(null)) {
            errors.rejectValue("assignedValue", "assignedValue.invalid"/*, arg2*/);
        } else {
            if(Stream.of(acceptedValues).map(x -> x.toUpperCase()).anyMatch(mma.getAssignedValue()::equals)){
                errors.rejectValue("assignedValue", "assignedValue.invalid"/*, arg2*/);
            }
            //if (!Arrays.stream(acceptedValues).anyMatch(mma.getAssignedValue()::equals)){
            
        }
        
        // check that assignedValue type is of type contained in ManagedAttributeType

    }
}
