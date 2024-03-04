package ca.gc.aafc.objectstore.api.repository;

import ca.gc.aafc.dina.util.UUIDHelper;
import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.dto.DerivativeDto;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;
import ca.gc.aafc.objectstore.api.testsupport.fixtures.DerivativeTestFixture;
import io.crnk.core.queryspec.QuerySpec;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import javax.inject.Inject;
import javax.validation.ValidationException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DerivativeRepositoryCRUDIT extends BaseIntegrationTest {

  @Inject
  private DerivativeRepository derivativeRepository;

  private ObjectUpload uploadTest_1;
  private ObjectStoreMetadata acDerivedFrom;

  @BeforeEach
  void setUp() {
    uploadTest_1 = ObjectUploadFactory.newObjectUpload()
      .isDerivative(true)
      .evaluatedFileExtension(MediaType.IMAGE_JPEG_VALUE)
      .build();

    objectUploadService.create(uploadTest_1);

    ObjectUpload uploadTest_2 = ObjectUploadFactory.newObjectUpload().build();

    objectUploadService.create(uploadTest_2);

    acDerivedFrom = ObjectStoreMetadataFactory.newObjectStoreMetadata()
      .fileIdentifier(uploadTest_2.getFileIdentifier()).build();
    objectStoreMetaDataService.create(acDerivedFrom);
  }
  
  @Test
  void create() {
    DerivativeDto resource = derivativeRepository.create(newDerivative(uploadTest_1.getFileIdentifier()));
    DerivativeDto result = derivativeRepository.findOne(
      resource.getUuid(),
      new QuerySpec(DerivativeDto.class));
    Assertions.assertEquals(resource.getDcType(), result.getDcType());
    Assertions.assertEquals(resource.getFileIdentifier(), result.getFileIdentifier());
    Assertions.assertEquals(resource.getDerivativeType(), result.getDerivativeType());
    Assertions.assertEquals(resource.getDcFormat(), result.getDcFormat());
    Assertions.assertEquals(uploadTest_1.getEvaluatedMediaType(), result.getDcFormat());
    // Auto generated fields
    Assertions.assertNotNull(result.getBucket());
    Assertions.assertNotNull(result.getFileExtension());
    Assertions.assertNotNull(result.getAcHashValue());
    Assertions.assertNotNull(result.getAcHashFunction());
    Assertions.assertNotNull(result.getCreatedBy());
  }

  @Test
  void create_WhenNoFileId_ThrowsValidationException() {
    DerivativeDto noFileId = newDerivative(uploadTest_1.getFileIdentifier());
    noFileId.setFileIdentifier(null);
    Assertions.assertThrows(ValidationException.class, () -> derivativeRepository.create(noFileId));
  }

  @Test
  void create_WhenNoObjectUpload_ThrowsValidationException() {
    Assertions.assertThrows(
      ValidationException.class,
      () -> derivativeRepository.create(newDerivative(UUIDHelper.generateUUIDv7())));
  }

  @Test
  void create_WhenNotDerivative_ThrowsBadRequest() {
    ObjectUpload notDerivative = ObjectUploadFactory.newObjectUpload().build();
    notDerivative.setIsDerivative(false);
    objectUploadService.create(notDerivative);
    Assertions.assertThrows(
      ValidationException.class,
      () -> derivativeRepository.create(newDerivative(notDerivative.getFileIdentifier())));
  }

  @Test
  void save() {
    DerivativeDto toCreate = newDerivative(uploadTest_1.getFileIdentifier());
    toCreate.setPubliclyReleasable(false);
    DerivativeDto resource = derivativeRepository.create(toCreate);
    DerivativeDto result = derivativeRepository.findOne(
      resource.getUuid(),
      new QuerySpec(DerivativeDto.class));
    assertFalse(result.getPubliclyReleasable());

    resource.setPubliclyReleasable(true);
    derivativeRepository.save(resource);

    result = derivativeRepository.findOne(
      resource.getUuid(),
      new QuerySpec(DerivativeDto.class));
    assertTrue(result.getPubliclyReleasable());
  }

  private DerivativeDto newDerivative(UUID fileIdentifier) {
    ObjectStoreMetadataDto from = new ObjectStoreMetadataDto();
    from.setUuid(acDerivedFrom.getUuid());

    DerivativeDto dto = DerivativeTestFixture.newDerivative(fileIdentifier);
    dto.setAcDerivedFrom(from);
    return dto;
  }
}
