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
    objectUpload = ObjectUploadFactory.newObjectUpload().build();
    objectUpload.setBucket("test");
    objectUpload.setEvaluatedMediaType(MediaType.IMAGE_JPEG_VALUE);
    this.service.save(objectUpload);
    this.service.save(acDerivedFrom);
  }

  @Test
  void create_WhenThumbnailSupported_ThumbnailGenerated() {
    Derivative derivative = newDerivative(acDerivedFrom);
    derivativeService.create(derivative);

    Derivative thumbResult = findAllByDerivative(derivative.getUuid())
      .stream().findFirst()
      .orElseGet(() -> Assertions.fail("A derivative for a thumbnail should of been generated"));

    Assertions.assertEquals(derivative.getBucket(), thumbResult.getBucket());
    Assertions.assertEquals(derivative.getUuid(), thumbResult.getGeneratedFromDerivative());
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
    Assertions.assertEquals(0, findAllByDerivative(derivative.getUuid()).size());
  }

  @Test
  void create_WhenThumbNailAlreadyExists_ThumbNailNotGenerated() {
    //TODO Check a thumbnail for original has not already been generated

  }

  @Test
  void generateThumbnail_DerivedFromDerivative_DerivativeGenerated() {
    Derivative derivative = newDerivative(null);
    this.service.save(derivative);

    UUID expectedUUID = derivative.getUuid();
    String bucket = "test";

    derivativeService.generateThumbnail(
      bucket,
      UUID.randomUUID() + ".jpg",
      MediaType.IMAGE_JPEG_VALUE,
      null,
      expectedUUID);

    Derivative thumbResult = findAllByDerivative(expectedUUID)
      .stream().findFirst()
      .orElseGet(() -> Assertions.fail("A derivative for a thumbnail should of been generated"));

    Assertions.assertEquals(bucket, thumbResult.getBucket());
    Assertions.assertEquals(expectedUUID, thumbResult.getGeneratedFromDerivative());
  }

  @Test
  void generateThumbnail_DerivedFromMetaData_DerivativeGenerated() {
    derivativeService.generateThumbnail(
      "test",
      UUID.randomUUID() + ".jpg",
      MediaType.IMAGE_JPEG_VALUE,
      acDerivedFrom.getUuid(),
      null);

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
        MediaType.IMAGE_JPEG_VALUE,
        null,
        UUID.randomUUID()));
  }

  @Test
  void generateThumbnail_WhenNoSource_ThrowsIllegalArgumentException() {
    Assertions.assertThrows(
      IllegalArgumentException.class,
      () -> derivativeService.generateThumbnail(
        "test",
        "dina.jpg",
        MediaType.IMAGE_JPEG_VALUE,
        null,
        null));
  }

  private Derivative newDerivative(ObjectStoreMetadata child) {
    return Derivative.builder()
      .uuid(UUID.randomUUID())
      .generatedFromDerivative(UUID.randomUUID())
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

  private List<Derivative> findAllByDerivative(UUID generatedFromDerivativeUUID) {
    return derivativeService.findAll(
      Derivative.class, (criteriaBuilder, derivativeRoot) -> new Predicate[]{
        criteriaBuilder.equal(derivativeRoot.get("generatedFromDerivative"), generatedFromDerivativeUUID),
      }, null, 0, 1);
  }

}
