package ca.gc.aafc.objectstore.api.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute.ManagedAttributeType;
import ca.gc.aafc.objectstore.api.testsupport.factories.ManagedAttributeFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.MetadataManagedAttributeFactory;



public class MetadataManagedAttributeValidatorTest {

    private MetadataManagedAttribute testMetadataManagedAttribute;
    private ManagedAttribute testManagedAttribute; 
    private MetadataManagedAttributeValidator validator = new MetadataManagedAttributeValidator();


    @Test
    public void assignedValueContainedInAcceptedValues_validationPasses() throws Exception {
        testManagedAttribute = ManagedAttributeFactory.newManagedAttribute()
            .name("test_attribute")
            .acceptedValues(new String[] {"val1", "val2"})
            .build();

        testMetadataManagedAttribute = MetadataManagedAttributeFactory
            .newMetadataManagedAttribute()
            .managedAttribute(testManagedAttribute)
            .assignedValue("val1")
            .build();
        Errors errors = new BeanPropertyBindingResult(testMetadataManagedAttribute, "testMetadataManagedAttribute");
        ValidationUtils.invokeValidator(validator, testMetadataManagedAttribute, errors);
        assertFalse(errors.hasFieldErrors());
    }

    @Test
    public void assignedValueNotContainedInAcceptedValues_validationFails() throws Exception {
        testManagedAttribute = ManagedAttributeFactory.newManagedAttribute()
            .name("test_attribute")
            .acceptedValues(new String[] {"val1", "val2"})
            .build();
        testMetadataManagedAttribute = MetadataManagedAttributeFactory
            .newMetadataManagedAttribute()
            .managedAttribute(testManagedAttribute)
            .assignedValue("val3")
            .build();
        Errors errors = new BeanPropertyBindingResult(testMetadataManagedAttribute, "mma");
        ValidationUtils.invokeValidator(validator, testMetadataManagedAttribute, errors);
        assertTrue(errors.hasFieldErrors());
        assertTrue(errors.hasFieldErrors("assignedValue"));
    }
    
    @Test
    public void acceptedValuesEmpty_assignedValueIsManagedAttributeType_validationPasses() throws Exception {
        testManagedAttribute = ManagedAttributeFactory.newManagedAttribute()
            .name("test_attribute")
            .build();

        testMetadataManagedAttribute = MetadataManagedAttributeFactory
            .newMetadataManagedAttribute()
            .managedAttribute(testManagedAttribute)
            .assignedValue("1234")
            .build();
        Errors errors = new BeanPropertyBindingResult(testMetadataManagedAttribute, "mma2");
        ValidationUtils.invokeValidator(validator, testMetadataManagedAttribute, errors);
        assertFalse(errors.hasFieldErrors());
        
    }

    @Test
    public void acceptedValuesEmpty_assignedValueNotManagedAttributeType_validationFails() {
        testManagedAttribute = ManagedAttributeFactory.newManagedAttribute()
            .name("test_attribute")
            .managedAttributeType(ManagedAttributeType.INTEGER)
            .build();

        testMetadataManagedAttribute = MetadataManagedAttributeFactory
            .newMetadataManagedAttribute()
            .managedAttribute(testManagedAttribute)
            .assignedValue("abcd")
            .build();

        Errors errors = new BeanPropertyBindingResult(testMetadataManagedAttribute, "mma3");
        ValidationUtils.invokeValidator(validator, testMetadataManagedAttribute, errors);
        assertTrue(errors.hasFieldErrors());
        assertTrue(errors.hasFieldErrors("assignedValue"));

    }

}


