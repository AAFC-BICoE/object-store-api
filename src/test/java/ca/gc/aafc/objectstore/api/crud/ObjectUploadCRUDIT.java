package ca.gc.aafc.objectstore.api.crud;

import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

public class ObjectUploadCRUDIT extends BaseEntityCRUDIT {

  private ObjectUpload objectUploadUnderTest = ObjectUploadFactory.newObjectUpload()
    .build();

  @Override
  public void testSave() {
    assertNull(objectUploadUnderTest.getId());
    objectUploadService.create(objectUploadUnderTest);
    assertNotNull(objectUploadUnderTest.getId());
  }

  @Override
  public void testFind() {
    ObjectUpload fetchedAcSubtype = objectUploadService.findOne(
      objectUploadUnderTest.getFileIdentifier(), ObjectUpload.class);
    assertEquals(objectUploadUnderTest.getId(), fetchedAcSubtype.getId());
    assertEquals(objectUploadUnderTest.getFileIdentifier(), fetchedAcSubtype.getFileIdentifier());
    assertEquals(objectUploadUnderTest.getCreatedBy(), fetchedAcSubtype.getCreatedBy());
    assertEquals(objectUploadUnderTest.getIsDerivative(), fetchedAcSubtype.getIsDerivative());
  }

  @Override
  public void testRemove() {
    UUID uuid = objectUploadUnderTest.getFileIdentifier();
    objectUploadService.delete(objectUploadUnderTest);
    assertNull(objectUploadService.findOne(
      uuid, ObjectUpload.class));
  }

}
