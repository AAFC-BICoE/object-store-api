package ca.gc.aafc.objectstore.api.testsupport.fixtures;

import ca.gc.aafc.objectstore.api.dto.ObjectUploadDto;

import java.util.UUID;

public class ObjectUploadTestFixture {

  public static ObjectUploadDto newObjectUpload() {
    ObjectUploadDto dto = new ObjectUploadDto();
    dto.setFileIdentifier(UUID.randomUUID());
    return dto;
  }
}
