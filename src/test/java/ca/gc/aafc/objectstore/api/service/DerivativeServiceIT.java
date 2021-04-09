package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.file.ThumbnailService;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import javax.inject.Inject;
import javax.persistence.criteria.Predicate;
import java.util.List;
import java.util.UUID;

public class DerivativeServiceIT extends BaseIntegrationTest {
  @Inject
  private DerivativeService derivativeService;
  private ObjectStoreMetadata acDerivedFrom;
  private ObjectUpload objectUpload;

  @BeforeEach
  void setUp() {
    acDerivedFrom = ObjectStoreMetadataFactory.newObjectStoreMetadata().build();
    objectUpload = ObjectUploadFactory.newObjectUpload()
      .bucket("test")
      .evaluatedMediaType(MediaType.IMAGE_JPEG_VALUE)
      .build();
    this.service.save(objectUpload);
    this.service.save(acDerivedFrom);
  }

  @Test
  void create_WhenThumbnailSupported_ThumbnailGenerated() {
    Derivative derivative = newDerivative(acDerivedFrom);
    derivativeService.create(derivative);

    Derivative thumbResult = findAllByDerivative(derivative)
      .stream().findFirst()
      .orElseGet(() -> Assertions.fail("A derivative for a thumbnail should of been generated"));

    Assertions.assertEquals(derivative.getBucket(), thumbResult.getBucket());
    Assertions.assertEquals(derivative.getUuid(), thumbResult.getGeneratedFromDerivative().getUuid());
    Assertions.assertEquals(acDerivedFrom.getUuid(), thumbResult.getAcDerivedFrom().getUuid());
    Assertions.assertEquals(ThumbnailService.SYSTEM_GENERATED, thumbResult.getCreatedBy());
    Assertions.assertEquals(ThumbnailService.THUMBNAIL_DC_TYPE, thumbResult.getDcType());
    Assertions.assertEquals(ThumbnailService.THUMBNAIL_EXTENSION, thumbResult.getFileExtension());
    Assertions.assertEquals(Derivative.DerivativeType.THUMBNAIL_IMAGE, thumbResult.getDerivativeType());
  }

  @Test
  void create_WhenDerivativeIsThumbNail_ThumbNailNotGenerated() {
    Derivative derivative = newDerivative(acDerivedFrom);
    derivative.setDerivativeType(Derivative.DerivativeType.THUMBNAIL_IMAGE);
    derivativeService.create(derivative);
    Assertions.assertEquals(0, findAllByDerivative(derivative).size());
  }

  @Test
  void create_WhenThumbNailAlreadyExists_ThumbNailNotGenerated() {
    Derivative derivative = newDerivative(acDerivedFrom);
    derivativeService.create(derivative);

    ObjectUpload upload = ObjectUploadFactory.newObjectUpload()
      .bucket("test").evaluatedMediaType(MediaType.IMAGE_JPEG_VALUE).build();
    this.service.save(upload);
    Derivative derivative2 = newDerivative(acDerivedFrom);
    derivative2.setFileIdentifier(upload.getFileIdentifier());
    derivativeService.create(derivative2);

    Assertions.assertEquals(1, findAllByDerivative(derivative).size());
    Assertions.assertEquals(0, findAllByDerivative(derivative2).size());
  }

  @Test
  void generateThumbnail_DerivedFromMetaData_DerivativeGenerated() {
    derivativeService.generateThumbnail(
      "test",
      UUID.randomUUID() + ".jpg",
      acDerivedFrom.getUuid(),
      MediaType.IMAGE_JPEG_VALUE,
      null,
      true);

    Derivative thumbNailDerivativeResult = derivativeService.findAll(
      Derivative.class, (criteriaBuilder, derivativeRoot) -> new Predicate[]{
        criteriaBuilder.equal(derivativeRoot.get("acDerivedFrom"), acDerivedFrom),
        criteriaBuilder.equal(derivativeRoot.get("derivativeType"), Derivative.DerivativeType.THUMBNAIL_IMAGE)
      }, null, 0, 1)
      .stream().findFirst()
      .orElseGet(() -> Assertions.fail("A derivative for a thumbnail should of been generated"));

    Assertions.assertEquals(acDerivedFrom.getUuid(), thumbNailDerivativeResult.getAcDerivedFrom().getUuid());
  }

  @Test
  void generateThumbnail_WhenGeneratedDerivedDoesNotExist_ThrowsIllegalArgumentException() {
    Assertions.assertThrows(
      IllegalArgumentException.class,
      () -> derivativeService.generateThumbnail(
        "test",
        "dina.jpg",
        acDerivedFrom.getUuid(),
        MediaType.IMAGE_JPEG_VALUE,
        UUID.randomUUID(),
        true));
  }

  private Derivative newDerivative(ObjectStoreMetadata child) {
    return Derivative.builder()
      .uuid(UUID.randomUUID())
      .fileIdentifier(objectUpload.getFileIdentifier())
      .fileExtension(".jpg")
      .bucket("mybucket")
      .acHashValue("abc")
      .acHashFunction("abcFunction")
      .dcType(DcType.IMAGE)
      .createdBy(RandomStringUtils.random(4))
      .acDerivedFrom(child)
      .derivativeType(Derivative.DerivativeType.LARGE_IMAGE)
      .build();
  }

  private List<Derivative> findAllByDerivative(Derivative derivative) {
    return derivativeService.findAll(
      Derivative.class, (criteriaBuilder, derivativeRoot) -> new Predicate[]{
        criteriaBuilder.equal(derivativeRoot.get("generatedFromDerivative"), derivative),
      }, null, 0, 1);
  }

}
