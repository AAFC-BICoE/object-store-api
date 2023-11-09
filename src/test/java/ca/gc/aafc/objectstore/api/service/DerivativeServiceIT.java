package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.file.ThumbnailGenerator;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import javax.persistence.criteria.Predicate;
import javax.validation.ValidationException;
import java.util.List;
import java.util.UUID;

public class DerivativeServiceIT extends BaseIntegrationTest {

  private ObjectStoreMetadata acDerivedFrom;
  private ObjectUpload objectUpload;

  @BeforeEach
  void setUp() {
    acDerivedFrom = ObjectStoreMetadataFactory.newObjectStoreMetadata().build();
    objectUpload = ObjectUploadFactory.newObjectUpload()
      .bucket("test")
      .isDerivative(true)
      .evaluatedMediaType(MediaType.IMAGE_JPEG_VALUE)
      .build();

    service.runInNewTransaction(em -> {
      em.persist(acDerivedFrom);
      em.persist(objectUpload);
    });
  }

  @AfterEach
  void tearDown() {
    service.runInNewTransaction(em -> {
      // find and delete since the entity is unmanaged in that new transaction
      em.remove(em.find(ObjectUpload.class, objectUpload.getId()));
    });
  }

  @Test
  void create_WhenThumbnailSupported_ThumbnailGenerated() {
    Derivative derivative = newDerivative(acDerivedFrom, objectUpload.getFileIdentifier());
    derivativeService.create(derivative);

    Derivative thumbResult = findAllByDerivative(derivative)
      .stream().findFirst()
      .orElseGet(() -> Assertions.fail("A derivative for a thumbnail should of been generated"));

    Assertions.assertEquals(derivative.getBucket(), thumbResult.getBucket());
    Assertions.assertEquals(derivative.getUuid(), thumbResult.getGeneratedFromDerivative().getUuid());
    Assertions.assertEquals(acDerivedFrom.getUuid(), thumbResult.getAcDerivedFrom().getUuid());
    Assertions.assertEquals(ThumbnailGenerator.SYSTEM_GENERATED, thumbResult.getCreatedBy());
    Assertions.assertEquals(ThumbnailGenerator.THUMBNAIL_DC_TYPE, thumbResult.getDcType());
    Assertions.assertEquals(ThumbnailGenerator.THUMBNAIL_EXTENSION, thumbResult.getFileExtension());
    Assertions.assertEquals(Derivative.DerivativeType.THUMBNAIL_IMAGE, thumbResult.getDerivativeType());
  }

  @Test
  void create_UsesValidator() {
    ObjectUpload upload = ObjectUploadFactory.newObjectUpload()
      .isDerivative(true)
      .bucket("test")
      .evaluatedMediaType(null).build();
    objectUploadService.create(upload);

    Derivative derivative = newDerivative(acDerivedFrom, upload.getFileIdentifier());
    derivative.setDerivativeType(Derivative.DerivativeType.THUMBNAIL_IMAGE);
    derivative.setDcFormat(null);
    Assertions.assertThrows(ValidationException.class, () -> derivativeService.create(derivative));
  }

  @Test
  void create_WhenDerivativeIsThumbNail_ThumbNailNotGenerated() {
    Derivative derivative = newDerivative(acDerivedFrom, objectUpload.getFileIdentifier());
    derivative.setDerivativeType(Derivative.DerivativeType.THUMBNAIL_IMAGE);
    derivativeService.create(derivative);
    Assertions.assertEquals(0, findAllByDerivative(derivative).size());
  }

  @Test
  void create_WhenThumbNailAlreadyExists_ThumbNailNotGenerated() {
    Derivative derivative = newDerivative(acDerivedFrom, objectUpload.getFileIdentifier());
    derivativeService.create(derivative);

    ObjectUpload upload = ObjectUploadFactory.newObjectUpload()
      .isDerivative(true)
      .bucket("test")
      .evaluatedMediaType(MediaType.IMAGE_JPEG_VALUE).build();
    objectUploadService.create(upload);
    Derivative derivative2 = newDerivative(acDerivedFrom, upload.getFileIdentifier());
    derivativeService.create(derivative2);

    Assertions.assertEquals(1, findAllByDerivative(derivative).size());
    Assertions.assertEquals(0, findAllByDerivative(derivative2).size());
  }

  @Test
  void update_UsesValidator() {
    ObjectUpload ou = ObjectUploadFactory.newObjectUpload()
      .bucket("test")
      .isDerivative(true)
      .evaluatedMediaType(null)
      .build();

    objectUploadService.create(ou);

    Derivative derivative = derivativeService.create(newDerivative(acDerivedFrom, ou.getFileIdentifier()));
    derivative.setDerivativeType(Derivative.DerivativeType.THUMBNAIL_IMAGE);
    derivative.setDcFormat(null);
    Assertions.assertThrows(ValidationException.class, () -> derivativeService.update(derivative));
  }

  @Test
  void generateThumbnail_DerivedFromMetaData_DerivativeGenerated() {
    derivativeService.generateThumbnail(
      "test",
      UUID.randomUUID() + ".jpg",
      acDerivedFrom.getUuid(),
      MediaType.IMAGE_JPEG_VALUE,
      null,
      true,
      acDerivedFrom.getPubliclyReleasable());

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
        true,
        acDerivedFrom.getPubliclyReleasable()));
  }

  @Test
  void generateThumbnail_WhenAcDerivedFromDoesNotExist_ThrowsIllegalArgumentException() {
    Assertions.assertThrows(
      IllegalArgumentException.class,
      () -> derivativeService.generateThumbnail(
        "test",
        "dina.jpg",
        UUID.randomUUID(),
        MediaType.IMAGE_JPEG_VALUE,
        null,
        true,
        acDerivedFrom.getPubliclyReleasable()));
  }

  private Derivative newDerivative(ObjectStoreMetadata child, UUID fileIdentifier) {
    return Derivative.builder()
      .uuid(UUID.randomUUID())
      .fileIdentifier(fileIdentifier)
      .fileExtension(".jpg")
      .bucket("mybucket")
      .dcFormat(MediaType.IMAGE_JPEG_VALUE)
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
