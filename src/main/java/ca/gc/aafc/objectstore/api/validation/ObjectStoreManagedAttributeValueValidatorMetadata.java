package ca.gc.aafc.objectstore.api.validation;

import static ca.gc.aafc.objectstore.api.config.ObjectStoreVocabularyConfiguration.MANAGED_ATTRIBUTE_VOCAB_UUID;

import java.util.UUID;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import ca.gc.aafc.dina.service.ControlledVocabularyItemService;
import ca.gc.aafc.dina.service.PostgresJsonbService;
import ca.gc.aafc.dina.validation.ManagedAttributeValueValidatorV2;
import ca.gc.aafc.objectstore.api.config.ObjectStoreVocabularyConfiguration;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreControlledVocabularyItem;
import jakarta.inject.Named;
import lombok.NonNull;

/**
 * For METADATA managed attribute
 */
@Component
public class ObjectStoreManagedAttributeValueValidatorMetadata extends ManagedAttributeValueValidatorV2<ObjectStoreControlledVocabularyItem> {

  public static final String METADATA_TABLE_NAME = "metadata";
  public static final String MANAGED_ATTRIBUTES_COL_NAME = "managed_attribute_values";

  private final PostgresJsonbService jsonbService;

  public ObjectStoreManagedAttributeValueValidatorMetadata(@Named("validationMessageSource")MessageSource messageSource,
                                                                @NonNull ControlledVocabularyItemService<ObjectStoreControlledVocabularyItem> vocabItemService,
                                                                PostgresJsonbService jsonbService) {
    super(messageSource, vocabItemService);
    this.jsonbService = jsonbService;
  }

  @Override
  public UUID getControlledVocabularyUuid() {
    return MANAGED_ATTRIBUTE_VOCAB_UUID;
  }

  @Override
  public String getDinaComponent() {
    return ObjectStoreVocabularyConfiguration.DinaComponent.METADATA.name();
  }

  @Override
  public boolean canBeDeleted(ObjectStoreControlledVocabularyItem controlledVocabularyItem) {
    return jsonbService.countFirstLevelKeys(
      METADATA_TABLE_NAME, MANAGED_ATTRIBUTES_COL_NAME, controlledVocabularyItem.getKey()) ==
      0;
  }
}
