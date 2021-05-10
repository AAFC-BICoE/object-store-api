package ca.gc.aafc.objectstore.api.crud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectSubtypeFactory;

public class ObjectSubtypeCRUDIT extends BaseEntityCRUDIT {

  private ObjectSubtype objectSubtypeUnderTest = ObjectSubtypeFactory.newObjectSubtype()
    .createdBy("createdBy")
    .build();

  @Override
  public void testSave() {
    assertNull(objectSubtypeUnderTest.getId());
    objectSubTypeService.create(objectSubtypeUnderTest);
    assertNotNull(objectSubtypeUnderTest.getId());
  }

  @Override
  public void testFind() {
    ObjectSubtype fetchedAcSubtype = objectSubTypeService.findOne(
      objectSubtypeUnderTest.getUuid(), ObjectSubtype.class);
    assertEquals(objectSubtypeUnderTest.getId(), fetchedAcSubtype.getId());
    assertEquals(objectSubtypeUnderTest.getAcSubtype(), fetchedAcSubtype.getAcSubtype());
    assertEquals(objectSubtypeUnderTest.getCreatedBy(), fetchedAcSubtype.getCreatedBy());
    assertNotNull(fetchedAcSubtype.getCreatedOn());
  }

  @Override
  public void testRemove() {
    UUID uuid = objectSubtypeUnderTest.getUuid();
    objectSubTypeService.delete(objectSubtypeUnderTest);
    assertNull(objectSubTypeService.findOne(
      uuid, ObjectSubtype.class));
  }
}
