package ca.gc.aafc.objectstore.api.validation;

import jakarta.inject.Named;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import ca.gc.aafc.dina.validation.ControlledVocabularyItemValidator;

@Component
public class ObjectStoreControlledVocabularyItemValidator extends ControlledVocabularyItemValidator {
  public ObjectStoreControlledVocabularyItemValidator(@Named("validationMessageSource")
                                                     MessageSource messageSource) {
    super(messageSource);
  }
}
