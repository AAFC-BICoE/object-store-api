package ca.gc.aafc.objectstore.api.testsupport.fixtures;

import org.apache.commons.lang3.RandomStringUtils;

import ca.gc.aafc.dina.entity.ControlledVocabulary;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreControlledVocabularyDto;

public class ObjectStoreControlledVocabularyTestFixture {
  public static ObjectStoreControlledVocabularyDto newObjectStoreControlledVocabulary() {
    ObjectStoreControlledVocabularyDto collectionControlledVocabularyItemDto = new ObjectStoreControlledVocabularyDto();
    collectionControlledVocabularyItemDto.setName(RandomStringUtils.randomAlphabetic(5));
    collectionControlledVocabularyItemDto.setType(ControlledVocabulary.ControlledVocabularyType.SYSTEM);
    collectionControlledVocabularyItemDto.setVocabClass(ControlledVocabulary.ControlledVocabularyClass.QUALIFIED_VALUE);
    collectionControlledVocabularyItemDto.setCreatedBy("created by");
    collectionControlledVocabularyItemDto.setMultilingualDescription(MultilingualTestFixture.newMultilingualDescription());
    return collectionControlledVocabularyItemDto;
  }

}
