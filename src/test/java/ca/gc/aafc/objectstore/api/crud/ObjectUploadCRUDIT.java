package ca.gc.aafc.objectstore.api.crud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.springframework.test.context.ActiveProfiles;

import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;

public class ObjectUploadCRUDIT extends BaseEntityCRUDIT {

  private ObjectUpload objectUploadUnderTest = ObjectUploadFactory.newObjectUpload()
    .build();

  @Override
  public void testSave() {
    assertNull(objectUploadUnderTest.getId());
    service.save(objectUploadUnderTest);
    assertNotNull(objectUploadUnderTest.getId());
  }

  @Override
  public void testFind() {
    ObjectUpload fetchedAcSubtype = service.find(ObjectUpload.class,
        objectUploadUnderTest.getId());
    assertEquals(objectUploadUnderTest.getId(), fetchedAcSubtype.getId());
    assertEquals(objectUploadUnderTest.getFileIdentifier(), fetchedAcSubtype.getFileIdentifier());
    assertEquals(objectUploadUnderTest.getCreatedBy(), fetchedAcSubtype.getCreatedBy());
  }

  @Override
  public void testRemove() {
    Integer id = objectUploadUnderTest.getId();
    service.deleteById(ObjectUpload.class, id);
    assertNull(service.find(ObjectUpload.class, id));
  }
}
