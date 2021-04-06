package ca.gc.aafc.objectstore.api.crud;

import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;

public class DerivativeCRUDIT extends BaseEntityCRUDIT {

  private Derivative derivative;
  private final ObjectStoreMetadata metadata = ObjectStoreMetadataFactory.newObjectStoreMetadata().build();

  @BeforeEach
  void setUp() {
    service.save(metadata);
    derivative = Derivative.builder()
      .uuid(UUID.randomUUID())
      .fileIdentifier(UUID.randomUUID())
      .fileExtension(".jpg")
      .bucket("mybucket")
      .acHashValue("abc")
      .acHashFunction("abcFunction")
      .dcType(DcType.IMAGE)
      .createdBy(RandomStringUtils.random(4))
      .acDerivedFrom(metadata)
      .derivativeType(Derivative.DerivativeType.THUMBNAIL_IMAGE)
      .build();
    service.save(derivative);
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
    service.deleteById(Derivative.class, id);
    assertNull(service.find(Derivative.class, id));
  }

}
