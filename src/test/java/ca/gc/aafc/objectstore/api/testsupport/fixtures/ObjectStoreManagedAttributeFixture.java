package ca.gc.aafc.objectstore.api.testsupport.fixtures;

import ca.gc.aafc.objectstore.api.dto.ObjectStoreManagedAttributeDto;
import ca.gc.aafc.objectstore.api.testsupport.factories.MultilingualDescriptionFactory;
import org.apache.commons.lang3.RandomStringUtils;

public class ObjectStoreManagedAttributeFixture {

  public static ObjectStoreManagedAttributeDto newObjectStoreManagedAttribute() {
    ObjectStoreManagedAttributeDto collectionManagedAttributeDto = new ObjectStoreManagedAttributeDto();
    collectionManagedAttributeDto.setName(RandomStringUtils.randomAlphabetic(5));
    collectionManagedAttributeDto.setAcceptedValues(new String[]{"value"});
    collectionManagedAttributeDto.setCreatedBy("created by");
    collectionManagedAttributeDto.setMultilingualDescription(
        MultilingualDescriptionFactory.newMultilingualDescription().build()
    );
    return collectionManagedAttributeDto;
  }
}
