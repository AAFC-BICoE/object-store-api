package ca.gc.aafc.objectstore.api.validation;

import org.junit.jupiter.api.Test;
import org.springframework.validation.ValidationUtils;

import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import ca.gc.aafc.objectstore.api.testsupport.factories.ManagedAttributeFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.MetadataManagedAttributeFactory;



public class MetadataManagedAttributeValidatorTest {

    private MetadataManagedAttribute metadataManagedAttribute;

    @Test
    public void assignedValueContainedInAcceptedValues_validationPasses() {
        metadataManagedAttribute = MetadataManagedAttributeFactory
            .newMetadataManagedAttribute()
            .build();
        //..
        ValidationUtils.invokeValidator(new MetadataManagedAttributeValidator(), metadataManagedAttribute, errors);
    }

    @Test
    public void assignedValueNotContainedInAcceptedValues_validationFails() {
        
    }
    
    @Test
    public void assignedValueIsManagedAttributeType_validationPasses() {
        
    }

    @Test
    public void assignedValueNotManagedAttributeType_validationFails() {
        
    }

    
}


