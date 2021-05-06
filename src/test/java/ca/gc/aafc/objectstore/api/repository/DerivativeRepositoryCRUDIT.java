package ca.gc.aafc.objectstore.api.repository;

import ca.gc.aafc.objectstore.api.dto.DerivativeDto;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.respository.DerivativeRepository;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.exception.MethodNotAllowedException;
import io.crnk.core.queryspec.QuerySpec;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import javax.inject.Inject;
import javax.validation.ValidationException;
import java.util.UUID;

public class DerivativeRepositoryCRUDIT extends BaseRepositoryTest {

  @Inject
  private DerivativeRepository derivativeRepository;
  private ObjectUpload uploadTest_1;
  private ObjectUpload uploadTest_2;
  private ObjectStoreMetadata acDerivedFrom;

  @BeforeEach
  void setUp() {
    uploadTest_1 = ObjectUploadFactory.newObjectUpload()
      .isDerivative(true)
      .evaluatedFileExtension(MediaType.IMAGE_JPEG_VALUE)
      .build();

    objectUploadService.create(uploadTest_1);

    uploadTest_2 = ObjectUploadFactory.newObjectUpload()
    .build();

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
      () -> derivativeRepository.create(newDerivative(UUID.randomUUID())));
  }

  @Test
  void create_WhenNotDerivative_ThrowsBadRequest() {
    ObjectUpload notDerivative = ObjectUploadFactory.newObjectUpload().build();
    notDerivative.setIsDerivative(false);
    objectUploadService.create(notDerivative);
    Assertions.assertThrows(
      BadRequestException.class,
      () -> derivativeRepository.create(newDerivative(notDerivative.getFileIdentifier())));
  }

  @Test
  void save_ThrowsMethodNotAllowed() {
    Assertions.assertThrows(
      MethodNotAllowedException.class,
      () -> derivativeRepository.save(newDerivative(UUID.randomUUID())));
  }

  private DerivativeDto newDerivative(UUID fileIdentifier) {
    DerivativeDto dto = new DerivativeDto();
    dto.setDcType(DcType.IMAGE);
    ObjectStoreMetadataDto from = new ObjectStoreMetadataDto();
    from.setUuid(acDerivedFrom.getUuid());
    dto.setAcDerivedFrom(from);
    dto.setDerivativeType(Derivative.DerivativeType.THUMBNAIL_IMAGE);
    dto.setFileIdentifier(fileIdentifier);
    return dto;
  }
}
