package ca.gc.aafc.objectstore.api.validation;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.validation.Validator;

import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import ca.gc.aafc.objectstore.api.testsupport.factories.ManagedAttributeFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.MetadataManagedAttributeFactory;



public class MetadataManagedAttributeValidatorTest {

    private MetadataManagedAttribute metadataManagedAttribute;

    @Inject 
    private MetadataManagedAttributeValidator validator;

    @Test
    public void assignedValueContainedInAcceptedValues_validationPasses() {
        metadataManagedAttribute = MetadataManagedAttributeFactory
            .newMetadataManagedAttribute()
            .build();
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


