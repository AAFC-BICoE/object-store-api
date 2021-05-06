package ca.gc.aafc.objectstore.api.crud;

import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.testsupport.factories.MetadataManagedAttributeFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

public class MetaManagedAttributeCRUDIT extends BaseEntityCRUDIT {

  private ObjectUpload objectUpload;

  private MetadataManagedAttribute attributeUnderTest;

  @BeforeEach
  void setUp() {
    objectUpload = ObjectUploadFactory.newObjectUpload().build();
    objectUploadService.create(objectUpload);
    attributeUnderTest = MetadataManagedAttributeFactory.newMetadataManagedAttribute().build();
    managedAttributeService.create(attributeUnderTest.getManagedAttribute());
    attributeUnderTest.getObjectStoreMetadata().setFileIdentifier(objectUpload.getFileIdentifier());
    objectStoreMetaDataService.create(attributeUnderTest.getObjectStoreMetadata());
    metaManagedAttributeService.create(attributeUnderTest);
  }

  @Override
  public void testSave() {
    Assertions.assertNotNull(attributeUnderTest.getId());
  }

  @Override
  public void testFind() {
    MetadataManagedAttribute fetchedAttribute = metaManagedAttributeService.findOne(
      attributeUnderTest.getUuid(), MetadataManagedAttribute.class);

    Assertions.assertNotNull(fetchedAttribute.getCreatedOn());
    Assertions.assertEquals(attributeUnderTest.getCreatedBy(), fetchedAttribute.getCreatedBy());
  }

  @Override
  public void testRemove() {
    UUID uuid = attributeUnderTest.getUuid();
    metaManagedAttributeService.delete(attributeUnderTest);
    Assertions.assertNull(metaManagedAttributeService.findOne(
      uuid, MetadataManagedAttribute.class));
  }

}
