package ca.gc.aafc.objectstore.api.service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.validation.SmartValidator;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.ControlledVocabularyItemService;
import ca.gc.aafc.dina.validation.ControlledVocabularyItemValidator;
import ca.gc.aafc.dina.validation.ManagedAttributeValueValidatorV2;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreControlledVocabularyItem;

/**
 * This class is responsible to check if a vocabulary item is used before deletion.
 * Deletion is still a risky operation since it is impossible to be 100% sure it is not used.
 */
@Service
public class ObjectStoreControlledVocabularyItemService extends ControlledVocabularyItemService<ObjectStoreControlledVocabularyItem> {

  // we are using ObjectProvider since it is a circular dependency, so it will be lazy initialized
  private final ObjectProvider<ManagedAttributeValueValidatorV2<ObjectStoreControlledVocabularyItem>> valueValidators;

  public ObjectStoreControlledVocabularyItemService(BaseDAO baseDAO, SmartValidator smartValidator,
                                                    ControlledVocabularyItemValidator itemValidator,
                                                    ObjectProvider<ManagedAttributeValueValidatorV2<ObjectStoreControlledVocabularyItem>> valueValidators) {
    super(baseDAO, smartValidator, ObjectStoreControlledVocabularyItem.class, itemValidator);
    this.valueValidators = valueValidators;
  }

  @Override
  protected void preDelete(ObjectStoreControlledVocabularyItem entity) {

    for (ManagedAttributeValueValidatorV2<ObjectStoreControlledVocabularyItem> valueValidator : valueValidators.stream()
      .toList()) {
      if (valueValidator.isApplicableTo(entity)) {
        if (!valueValidator.canBeDeleted(entity)) {
          throw new IllegalStateException(
            "Managed attribute key: " + entity.getKey() + ", is currently in use.");
        }
      }
    }
  }
}
