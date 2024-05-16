package ca.gc.aafc.objectstore.api.testsupport.fixtures;

import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;

public class ObjectStoreMetadataTestFixture {

  public static ObjectStoreMetadataDto newObjectStoreMetadata() {

    ObjectStoreMetadataDto osMetadata = new ObjectStoreMetadataDto();
    osMetadata.setAcHashFunction("SHA-1");

    return osMetadata;

  }
}
