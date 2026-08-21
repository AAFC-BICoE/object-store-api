package ca.gc.aafc.objectstore.api.testsupport.factories;

import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;

import ca.gc.aafc.dina.vocabulary.TypedVocabularyElement;
import ca.gc.aafc.objectstore.api.config.ObjectStoreVocabularyConfiguration.DinaComponent;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreControlledVocabularyItem;
import ca.gc.aafc.objectstore.api.testsupport.fixtures.MultilingualTestFixture;

public class ObjectStoreControlledVocabularyItemTestFactory {

    /**
   * Static method that can be called to return a configured builder that can be
   * further customized to return the actual entity object, call the .build()
   * method on a builder.
   *
   * @return Pre-configured builder with all mandatory fields set
   */
  public static ObjectStoreControlledVocabularyItem.ObjectStoreControlledVocabularyItemBuilder<?, ?> newObjectStoreControlledVocabularyItem() {
    return ObjectStoreControlledVocabularyItem
      .builder()
      .uuid(UUID.randomUUID())
      .name(RandomStringUtils.insecure().nextAlphabetic(5))
      .group(RandomStringUtils.insecure().nextAlphabetic(5))
      .createdBy(RandomStringUtils.insecure().nextAlphabetic(5))
      .vocabularyElementType(TypedVocabularyElement.VocabularyElementType.STRING)
      .acceptedValues(new String[]{"value"})
      .dinaComponent(DinaComponent.METADATA.name())
      .multilingualDescription(MultilingualTestFixture.newMultilingualDescription());
  }
}
