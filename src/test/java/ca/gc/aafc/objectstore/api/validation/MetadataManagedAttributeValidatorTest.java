package ca.gc.aafc.objectstore.api.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ValidationUtils;

import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute.ManagedAttributeType;
import ca.gc.aafc.objectstore.api.testsupport.factories.ManagedAttributeFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.MetadataManagedAttributeFactory;



public class MetadataManagedAttributeValidatorTest {
  
  private ManagedAttribute testManagedAttribute;
  private MetadataManagedAttribute testMetadataManagedAttribute;
  private static final ReloadableResourceBundleMessageSource messageSource = messageSource();
  private static final MetadataManagedAttributeValidator validatorUnderTest = new MetadataManagedAttributeValidator(messageSource);

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
    Errors errors = new BeanPropertyBindingResult(testMetadataManagedAttribute, "mma");
    ValidationUtils.invokeValidator(validatorUnderTest, testMetadataManagedAttribute, errors);
    assertFalse(errors.hasFieldErrors());
    assertFalse(errors.hasErrors());
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
    ValidationUtils.invokeValidator(validatorUnderTest, testMetadataManagedAttribute, errors);
    assertEquals(1, errors.getErrorCount());
    assertTrue(errors.hasFieldErrors("assignedValue"));
    FieldError field_error = errors.getFieldError("assignedValue");
    assertTrue(field_error.getCode().equals("assignedValue.invalid"));
    assertTrue(field_error.getDefaultMessage().contains("val3"));
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
    Errors errors = new BeanPropertyBindingResult(testMetadataManagedAttribute, "mma");
    ValidationUtils.invokeValidator(validatorUnderTest, testMetadataManagedAttribute, errors);
    assertFalse(errors.hasErrors());  
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

    Errors errors = new BeanPropertyBindingResult(testMetadataManagedAttribute, "mma");
    ValidationUtils.invokeValidator(validatorUnderTest, testMetadataManagedAttribute, errors);
    assertEquals(1, errors.getErrorCount());
    assertTrue(errors.hasFieldErrors("assignedValue"));
    FieldError field_error = errors.getFieldError("assignedValue");
    assertTrue(field_error.getCode().equals("assignedValue.invalid"));
    assertTrue(field_error.getDefaultMessage().contains("abcd"));
  }

  public static ReloadableResourceBundleMessageSource messageSource() {
    ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
    messageSource.setDefaultLocale(LocaleContextHolder.getLocale());
    messageSource.setBasename("classpath:validation-messages");
    messageSource.setDefaultEncoding("UTF-8");
    return messageSource;
  }
}


