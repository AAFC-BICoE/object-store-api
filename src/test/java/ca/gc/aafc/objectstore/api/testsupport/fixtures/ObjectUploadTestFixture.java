package ca.gc.aafc.objectstore.api.testsupport.fixtures;

import ca.gc.aafc.dina.util.UUIDHelper;
import ca.gc.aafc.objectstore.api.dto.ObjectUploadDto;

public class ObjectUploadTestFixture {

  public static ObjectUploadDto newObjectUpload() {
    ObjectUploadDto dto = new ObjectUploadDto();
    dto.setFileIdentifier(UUIDHelper.generateUUIDv7());
    return dto;
  }
}
