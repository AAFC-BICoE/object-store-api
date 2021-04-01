package ca.gc.aafc.objectstore.api.repository;

import ca.gc.aafc.objectstore.api.dto.DerivativeDto;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.respository.DerivativeRepository;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.exception.MethodNotAllowedException;
import io.crnk.core.queryspec.QuerySpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.validation.ValidationException;
import java.util.UUID;

public class DerivativeRepositoryCRUDIT extends BaseRepositoryTest {

  @Inject
  private DerivativeRepository derivativeRepository;
  private ObjectUpload upload;

  @BeforeEach
  void setUp() {
    upload = ObjectUploadFactory.newObjectUpload().build();
    upload.setIsDerivative(true);
    this.service.save(upload);
  }

  @Test
  void create() {
    DerivativeDto resource = derivativeRepository.create(newDerivative(upload.getFileIdentifier()));
    DerivativeDto result = derivativeRepository.findOne(
      resource.getUuid(),
      new QuerySpec(DerivativeDto.class));
    Assertions.assertEquals(resource.getDcType(), result.getDcType());
    Assertions.assertEquals(resource.getFileIdentifier(), result.getFileIdentifier());
    // Auto generated fields
    Assertions.assertNotNull(result.getBucket());
    Assertions.assertNotNull(result.getFileExtension());
    Assertions.assertNotNull(result.getAcHashValue());
    Assertions.assertNotNull(result.getAcHashFunction());
    Assertions.assertNotNull(result.getCreatedBy());
  }

  @Test
  void create_WhenNoFileId_ThrowsValidationException() {
    DerivativeDto noFileId = newDerivative(upload.getFileIdentifier());
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
    this.service.save(notDerivative);
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

  private static DerivativeDto newDerivative(UUID fileIdentifier) {
    DerivativeDto dto = new DerivativeDto();
    dto.setDcType(DcType.IMAGE);
    dto.setFileIdentifier(fileIdentifier);
    return dto;
  }
}
