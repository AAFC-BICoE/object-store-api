package ca.gc.aafc.objectstore.api.testsupport.fixtures;

import org.apache.commons.lang3.RandomStringUtils;

import ca.gc.aafc.dina.vocabulary.TypedVocabularyElement;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreControlledVocabularyItemDto;

public class ObjectStoreControlledVocabularyItemTestFixture {

  public static final String GROUP = "dina";

  public static ObjectStoreControlledVocabularyItemDto newObjectStoreControlledVocabularyItem() {
    ObjectStoreControlledVocabularyItemDto collectionControlledVocabularyItemDto = new ObjectStoreControlledVocabularyItemDto();
    collectionControlledVocabularyItemDto.setName(RandomStringUtils.randomAlphabetic(5));
    collectionControlledVocabularyItemDto.setVocabularyElementType(
      TypedVocabularyElement.VocabularyElementType.INTEGER);
    collectionControlledVocabularyItemDto.setAcceptedValues(new String[]{"1", "2"});
    collectionControlledVocabularyItemDto.setTerm("the-term");
    collectionControlledVocabularyItemDto.setUnit("cm");
    collectionControlledVocabularyItemDto.setCreatedBy("created by");
    collectionControlledVocabularyItemDto.setGroup("test");
    collectionControlledVocabularyItemDto.setUriTemplate("http://test.org/$1");
    collectionControlledVocabularyItemDto.setDinaComponent("abc");
    collectionControlledVocabularyItemDto.setMultilingualTitle(MultilingualTestFixture.newMultilingualTitle());
    collectionControlledVocabularyItemDto.setMultilingualDescription(MultilingualTestFixture.newMultilingualDescription());
    return collectionControlledVocabularyItemDto;
  }
}
