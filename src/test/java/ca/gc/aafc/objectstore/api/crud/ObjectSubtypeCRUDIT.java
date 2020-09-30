package ca.gc.aafc.objectstore.api.crud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectSubtypeFactory;

public class ObjectSubtypeCRUDIT extends BaseEntityCRUDIT {

  private ObjectSubtype objectSubtypeUnderTest = ObjectSubtypeFactory.newObjectSubtype()
    .createdBy("createdBy")
    .build();

  @Override
  public void testSave() {
    assertNull(objectSubtypeUnderTest.getId());
    service.save(objectSubtypeUnderTest);
    assertNotNull(objectSubtypeUnderTest.getId());
  }

  @Override
  public void testFind() {
    ObjectSubtype fetchedAcSubtype = service.find(ObjectSubtype.class,
        objectSubtypeUnderTest.getId());
    assertEquals(objectSubtypeUnderTest.getId(), fetchedAcSubtype.getId());
    assertEquals(objectSubtypeUnderTest.getAcSubtype(), fetchedAcSubtype.getAcSubtype());
    assertEquals(objectSubtypeUnderTest.getCreatedBy(), fetchedAcSubtype.getCreatedBy());
    assertNotNull(fetchedAcSubtype.getCreatedOn());
  }

  @Override
  public void testRemove() {
    Integer id = objectSubtypeUnderTest.getId();
    service.deleteById(ObjectSubtype.class, id);
    assertNull(service.find(ObjectSubtype.class, id));
  }
}
