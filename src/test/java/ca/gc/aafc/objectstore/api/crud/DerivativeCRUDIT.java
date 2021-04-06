package ca.gc.aafc.objectstore.api.crud;

import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.service.DerivativeService;
import ca.gc.aafc.objectstore.api.service.ObjectStoreMetaDataService;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.UUID;

public class DerivativeCRUDIT extends BaseEntityCRUDIT {

  @Inject
  private ObjectStoreMetaDataService metaService;
  @Inject
  private DerivativeService derivativeService;

  private Derivative derivative;
  private final ObjectStoreMetadata metadata = ObjectStoreMetadataFactory.newObjectStoreMetadata().build();

  @BeforeEach
  void setUp() {
    metaService.create(metadata);
    derivative = newDerivative(metadata);
    derivativeService.create(derivative);
  }

  @Override
  public void testSave() {
    Assertions.assertNotNull(derivative.getCreatedOn());
    Assertions.assertNotNull(derivative.getId());
  }

  @Override
  public void testFind() {
    Derivative result = service.find(Derivative.class, this.derivative.getId());
    Assertions.assertNotNull(result);
    Assertions.assertEquals(derivative.getUuid(), result.getUuid());
    Assertions.assertEquals(derivative.getBucket(), result.getBucket());
    Assertions.assertEquals(derivative.getFileIdentifier(), result.getFileIdentifier());
    Assertions.assertEquals(derivative.getFileExtension(), result.getFileExtension());
    Assertions.assertEquals(derivative.getAcHashFunction(), result.getAcHashFunction());
    Assertions.assertEquals(derivative.getAcHashValue(), result.getAcHashValue());
    Assertions.assertEquals(derivative.getCreatedBy(), result.getCreatedBy());
    Assertions.assertEquals(derivative.getCreatedOn(), result.getCreatedOn());
    Assertions.assertEquals(derivative.getDcType(), result.getDcType());
    Assertions.assertEquals(derivative.getDerivativeType(), result.getDerivativeType());
    Assertions.assertEquals(metadata.getUuid(), result.getAcDerivedFrom().getUuid());
  }

  @Override
  public void testRemove() {
    Integer id = derivative.getId();
    derivativeService.delete(derivative);
    Assertions.assertNull(service.find(Derivative.class, id));
  }

  /**
   * This test case is required because a child is currently only soft deleted, so we need to establish the
   * relation is removed.
   */
  @Test
  void delete_WhenChildDeleted_RelationRemoved() {
    ObjectStoreMetadata child = ObjectStoreMetadataFactory.newObjectStoreMetadata().build();
    metaService.create(child);

    Derivative parent = newDerivative(child);
    derivativeService.create(parent);

    metaService.delete(service.find(ObjectStoreMetadata.class, child.getId()));
    Derivative result = service.find(Derivative.class, parent.getId());

    Assertions.assertNull(result.getAcDerivedFrom());
  }

  private Derivative newDerivative(ObjectStoreMetadata child) {
    return Derivative.builder()
      .uuid(UUID.randomUUID())
      .fileIdentifier(UUID.randomUUID())
      .fileExtension(".jpg")
      .bucket("mybucket")
      .acHashValue("abc")
      .acHashFunction("abcFunction")
      .dcType(DcType.IMAGE)
      .createdBy(RandomStringUtils.random(4))
      .acDerivedFrom(child)
      .derivativeType(Derivative.DerivativeType.THUMBNAIL_IMAGE)
      .build();
  }
}
