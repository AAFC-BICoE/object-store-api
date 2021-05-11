package ca.gc.aafc.objectstore.api.validation;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import javax.inject.Inject;
import java.util.UUID;

class DerivativeValidatorTest extends BaseIntegrationTest {

  @Inject
  private DerivativeValidator validator;

  @Inject
  private MessageSource messageSource;

  @Test
  void supports() {
    Assertions.assertTrue(validator.supports(Derivative.class));
    Assertions.assertFalse(validator.supports(Integer.class));
  }

  @Test
  void validate_WhenValidDerivativeForThumbnail_ValidationSuccess() {
    Derivative derivative = newDerivative();

    Errors errors = new BeanPropertyBindingResult(derivative, derivative.getUuid().toString());
    validator.validate(derivative, errors);
    Assertions.assertEquals(0, errors.getAllErrors().size());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"  ", "\t", "\n"})
  void validate_WhenDerivativeForThumbnail_BlankDcFormat_ErrorsReturned(String input) {
    String expectedErrorMessage = getExpectedErrorMessage(DerivativeValidator.VALID_THUMB_DC_FORMAT_KEY);

    Derivative derivative = newDerivative();
    derivative.setDcFormat(input);

    Errors errors = new BeanPropertyBindingResult(derivative, derivative.getUuid().toString());
    validator.validate(derivative, errors);
    Assertions.assertEquals(1, errors.getAllErrors().size());
    Assertions.assertEquals(expectedErrorMessage, errors.getAllErrors().get(0).getDefaultMessage());
  }

  @Test
  void validate_WhenDerivativeForThumbnail_UnSupportedDcFormat_ErrorsReturned() {
    String expectedErrorMessage = getExpectedErrorMessage(DerivativeValidator.VALID_THUMB_SUPPORTED_KEY);

    Derivative derivative = newDerivative();
    derivative.setDcFormat(MediaType.TEXT_XML_VALUE);

    Errors errors = new BeanPropertyBindingResult(derivative, derivative.getUuid().toString());
    validator.validate(derivative, errors);
    Assertions.assertEquals(1, errors.getAllErrors().size());
    Assertions.assertEquals(expectedErrorMessage, errors.getAllErrors().get(0).getDefaultMessage());
  }

  private String getExpectedErrorMessage(String key) {
    return messageSource.getMessage(
      key,
      null,
      LocaleContextHolder.getLocale());
  }

  private static Derivative newDerivative() {
    return Derivative.builder()
      .uuid(UUID.randomUUID())
      .derivativeType(Derivative.DerivativeType.THUMBNAIL_IMAGE)
      .dcFormat(MediaType.IMAGE_JPEG_VALUE)
      .build();
  }
}