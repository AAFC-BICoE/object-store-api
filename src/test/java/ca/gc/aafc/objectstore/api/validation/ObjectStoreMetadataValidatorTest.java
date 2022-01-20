package ca.gc.aafc.objectstore.api.validation;

import java.util.UUID;
import javax.inject.Inject;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;

public class ObjectStoreMetadataValidatorTest extends BaseIntegrationTest {
  
  @Inject
  private ObjectStoreMetadataValidator validator;

  @Inject
  private MessageSource messageSource;

  @Test
  void supports() {
    Assertions.assertTrue(validator.supports(ObjectStoreMetadata.class));
    Assertions.assertFalse(validator.supports(Integer.class));
  }

  @Test
  void validate_fileIdentifierSet_NoErrorReturned() {
    // Create an object with both file identifier and external resource.
    ObjectStoreMetadata objectStoreMetadata = createMetadata();
    objectStoreMetadata.setFileIdentifier(UUID.randomUUID());

    Errors errors = new BeanPropertyBindingResult(objectStoreMetadata, objectStoreMetadata.getUuid().toString());
    validator.validate(objectStoreMetadata, errors);
    Assertions.assertEquals(0, errors.getAllErrors().size());
  }  

  @Test
  void validate_externalResourceIdentifierSet_NoErrorReturned() {
    // Create an object with both file identifier and external resource.
    ObjectStoreMetadata objectStoreMetadata = createMetadata();
    objectStoreMetadata.setResourceExternalURI("https://www." + RandomStringUtils.randomAlphabetic(10) + ".com");

    Errors errors = new BeanPropertyBindingResult(objectStoreMetadata, objectStoreMetadata.getUuid().toString());
    validator.validate(objectStoreMetadata, errors);
    Assertions.assertEquals(0, errors.getAllErrors().size());
  }  

  @Test
  void validate_fileIdentifierAndExternalResourceSet_ErrorReturned() {
    String expectedErrorMessage = getExpectedErrorMessage(ObjectStoreMetadataValidator.VALID_FILE_ID_OR_RESOURCE_EXTERNAL);

    // Create an object with both file identifier and external resource.
    ObjectStoreMetadata objectStoreMetadata = createMetadata();
    objectStoreMetadata.setFileIdentifier(UUID.randomUUID());
    objectStoreMetadata.setResourceExternalURI("https://www." + RandomStringUtils.randomAlphabetic(10) + ".com");

    Errors errors = new BeanPropertyBindingResult(objectStoreMetadata, objectStoreMetadata.getUuid().toString());
    validator.validate(objectStoreMetadata, errors);
    Assertions.assertEquals(1, errors.getAllErrors().size());
    Assertions.assertEquals(expectedErrorMessage, errors.getAllErrors().get(0).getDefaultMessage());
  }

  @Test
  void validate_fileIdentifierAndExternalResourceNotSet_ErrorReturned() {
    String expectedErrorMessage = getExpectedErrorMessage(ObjectStoreMetadataValidator.NO_FILE_ID_OR_RESOURCE_EXTERNAL);

    // Create a blank object.
    ObjectStoreMetadata objectStoreMetadata = createMetadata();

    Errors errors = new BeanPropertyBindingResult(objectStoreMetadata, objectStoreMetadata.getUuid().toString());
    validator.validate(objectStoreMetadata, errors);
    Assertions.assertEquals(1, errors.getAllErrors().size());
    Assertions.assertEquals(expectedErrorMessage, errors.getAllErrors().get(0).getDefaultMessage());
  }

  private String getExpectedErrorMessage(String key) {
    return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
  }

  private static ObjectStoreMetadata createMetadata() {
    return ObjectStoreMetadata.builder().uuid(UUID.randomUUID()).build();
  }
}
