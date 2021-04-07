package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.file.ThumbnailService;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import javax.inject.Inject;
import javax.persistence.criteria.Predicate;
import java.util.UUID;

public class DerivativeServiceIT extends BaseIntegrationTest {
  @Inject
  private DerivativeService derivativeService;

  @Test
  void generateThumbnail_DerivedFromDerivative_DerivativeGenerated() {
    //TODO we need to make sure a real derivative is present
    //TODO Check a thumbnail for original has not already been generated
    UUID generatedFromDerivativeUUID = UUID.randomUUID();
    String bucket = "test";

    derivativeService.generateThumbnail(
      bucket,
      UUID.randomUUID() + ".jpg",
      MediaType.IMAGE_JPEG_VALUE,
      null,
      generatedFromDerivativeUUID);

    Derivative thumbResult = derivativeService.findAll(
      Derivative.class, (criteriaBuilder, derivativeRoot) -> new Predicate[]{
        criteriaBuilder.equal(derivativeRoot.get("generatedFromDerivative"), generatedFromDerivativeUUID),
      }, null, 0, 1)
      .stream().findFirst()
      .orElseGet(() -> Assertions.fail("A derivative for a thumbnail should of been generated"));

    Assertions.assertEquals(bucket, thumbResult.getBucket());
    Assertions.assertEquals(generatedFromDerivativeUUID, thumbResult.getGeneratedFromDerivative());
    Assertions.assertEquals(ThumbnailService.SYSTEM_GENERATED, thumbResult.getCreatedBy());
    Assertions.assertEquals(ThumbnailService.THUMBNAIL_DC_TYPE, thumbResult.getDcType());
    Assertions.assertEquals(ThumbnailService.THUMBNAIL_EXTENSION, thumbResult.getFileExtension());
    Assertions.assertEquals(Derivative.DerivativeType.THUMBNAIL_IMAGE, thumbResult.getDerivativeType());
  }

  @Test
  void generateThumbnail_DerivedFromMetaData_DerivativeGenerated() {
    ObjectStoreMetadata metadata = ObjectStoreMetadataFactory.newObjectStoreMetadata().build();
    this.service.save(metadata);

    derivativeService.generateThumbnail(
      "test",
      UUID.randomUUID() + ".jpg",
      MediaType.IMAGE_JPEG_VALUE,
      metadata.getUuid(),
      null);

    Derivative thumbNailDerivativeResult = derivativeService.findAll(
      Derivative.class, (criteriaBuilder, derivativeRoot) -> new Predicate[]{
        criteriaBuilder.equal(derivativeRoot.get("acDerivedFrom"), metadata),
        criteriaBuilder.equal(derivativeRoot.get("derivativeType"), Derivative.DerivativeType.THUMBNAIL_IMAGE)
      }, null, 0, 1)
      .stream().findFirst()
      .orElseGet(() -> Assertions.fail("A derivative for a thumbnail should of been generated"));

    Assertions.assertEquals(metadata.getUuid(), thumbNailDerivativeResult.getAcDerivedFrom().getUuid());
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
}
