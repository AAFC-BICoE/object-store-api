package ca.gc.aafc.objectstore.api.crud;

import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.MediaType;

import java.util.UUID;

public class DerivativeCRUDIT extends BaseEntityCRUDIT {

  private Derivative derivative;

  private final ObjectStoreMetadata metadata = ObjectStoreMetadataFactory.newObjectStoreMetadata().build();
  private final ObjectUpload upload = ObjectUploadFactory.newObjectUpload().fileIdentifier(metadata.getFileIdentifier()).build();

  @BeforeEach
  void setUp() {

    objectUploadService.create(upload);
    objectStoreMetaDataService.create(metadata);

    Derivative generatedFrom = newDerivative(metadata);
    derivativeService.create(generatedFrom);

    derivative = newDerivative(metadata);
    derivative.setGeneratedFromDerivative(generatedFrom);
    derivativeService.create(derivative);
  }

  @Override
  public void testSave() {
    Assertions.assertNotNull(derivative.getCreatedOn());
    Assertions.assertNotNull(derivative.getId());
  }

  @Override
  public void testFind() {
    Derivative result = derivativeService.findOne(derivative.getUuid(), Derivative.class);
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
    Assertions.assertEquals(derivative.getDcFormat(), result.getDcFormat());
    Assertions.assertEquals(derivative.getDerivativeType(), result.getDerivativeType());
    Assertions.assertNotNull(result.getGeneratedFromDerivative());
    Assertions.assertEquals(derivative.getGeneratedFromDerivative(), result.getGeneratedFromDerivative());
    Assertions.assertEquals(metadata.getUuid(), result.getAcDerivedFrom().getUuid());
  }

  @Override
  public void testRemove() {
    UUID uuid = derivative.getUuid();
    derivativeService.delete(derivative);
    Assertions.assertNull(derivativeService.findOne(uuid, Derivative.class));
    // Child should still exist
    Assertions.assertNotNull(objectStoreMetaDataService.findOne(metadata.getUuid(), ObjectStoreMetadata.class));
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
      .dcFormat(MediaType.IMAGE_JPEG_VALUE)
      .createdBy(RandomStringUtils.random(4))
      .acDerivedFrom(child)
      .derivativeType(Derivative.DerivativeType.THUMBNAIL_IMAGE)
      .build();
  }
}
