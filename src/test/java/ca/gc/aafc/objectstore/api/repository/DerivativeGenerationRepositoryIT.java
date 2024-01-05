package ca.gc.aafc.objectstore.api.repository;

import javax.inject.Inject;
import javax.persistence.criteria.Predicate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.dto.DerivativeGenerationDto;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.testsupport.factories.DerivativeFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;

public class DerivativeGenerationRepositoryIT extends BaseIntegrationTest {

  @Inject
  private DerivativeGenerationRepository derivativeGenerationRepository;

  @Test
  public void generateThumbnail() {

    // create an object upload of type text to avoid the thumbnail creation
    ObjectUpload uploadOriginal = ObjectUploadFactory.newObjectUpload()
      .dcType(DcType.TEXT)
      .evaluatedFileExtension(ObjectUploadFactory.TEST_FILE_EXT)
      .evaluatedMediaType(ObjectUploadFactory.TEST_FILE_MEDIA_TYPE)
      .build();
    objectUploadService.create(uploadOriginal);
    ObjectStoreMetadata acDerivedFrom = ObjectStoreMetadataFactory.newObjectStoreMetadata()
      .fileIdentifier(uploadOriginal.getFileIdentifier()).build();
    objectStoreMetaDataService.create(acDerivedFrom);

    ObjectUpload uploadDerivative = ObjectUploadFactory.newObjectUpload()
      .isDerivative(true)
      .evaluatedFileExtension(MediaType.IMAGE_JPEG_VALUE)
      .build();
    objectUploadService.create(uploadDerivative);

    Derivative derivative = DerivativeFactory.newDerivative(acDerivedFrom, uploadDerivative.getFileIdentifier()).build();
    derivativeService.create(derivative);

    Derivative thumbResult = derivativeService.findAll(
      Derivative.class, (criteriaBuilder, derivativeRoot) -> new Predicate[]{
        criteriaBuilder.equal(derivativeRoot.get("generatedFromDerivative"), derivative),
      }, null, 0, 1)
      .stream().findFirst()
      .orElseGet(() -> Assertions.fail("A derivative for a thumbnail should of been generated"));

    derivativeService.delete(thumbResult);

    var dto = derivativeGenerationRepository.create(DerivativeGenerationDto.builder()
      .metadataUuid(acDerivedFrom.getUuid())
      .derivativeType(Derivative.DerivativeType.THUMBNAIL_IMAGE)
      .derivedFromType(Derivative.DerivativeType.LARGE_IMAGE).build());

    thumbResult = derivativeService.findAll(
        Derivative.class, (criteriaBuilder, derivativeRoot) -> new Predicate[]{
          criteriaBuilder.equal(derivativeRoot.get("generatedFromDerivative"), derivative),
        }, null, 0, 1)
      .stream().findFirst()
      .orElseGet(() -> Assertions.fail("A derivative for a thumbnail should of been generated"));

  }

}
