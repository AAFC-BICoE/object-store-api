package ca.gc.aafc.objectstore.api.crud;

import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import ca.gc.aafc.objectstore.api.testsupport.factories.MetadataManagedAttributeFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

public class MetaManagedAttributeCRUDIT extends BaseEntityCRUDIT {

  private MetadataManagedAttribute attributeUnderTest;

  @BeforeEach
  void setUp() {
    attributeUnderTest = MetadataManagedAttributeFactory.newMetadataManagedAttribute().build();
    service.save(attributeUnderTest.getManagedAttribute());
    service.save(attributeUnderTest.getObjectStoreMetadata());
    service.save(attributeUnderTest);
  }

  @Override
  public void testSave() {
    Assertions.assertNotNull(attributeUnderTest.getId());
  }

  @Override
  public void testFind() {
    MetadataManagedAttribute fetchedAttribute = service.find(
      MetadataManagedAttribute.class,
      this.attributeUnderTest.getId());

    Assertions.assertNotNull(fetchedAttribute.getCreatedOn());
    Assertions.assertEquals(attributeUnderTest.getCreatedBy(), fetchedAttribute.getCreatedBy());
  }

  @Override
  public void testRemove() {
    service.deleteById(MetadataManagedAttribute.class, attributeUnderTest.getId());
    Assertions.assertNull(service.find(MetadataManagedAttribute.class, attributeUnderTest.getId()));
  }

}
