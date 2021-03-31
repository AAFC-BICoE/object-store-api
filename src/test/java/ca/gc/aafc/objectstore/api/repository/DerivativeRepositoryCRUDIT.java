package ca.gc.aafc.objectstore.api.repository;

import ca.gc.aafc.objectstore.api.dto.DerivativeDto;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.respository.DerivativeRepository;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;
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
    Assertions.assertEquals(resource.getBucket(), result.getBucket());
  }

  @Test
  void create_WhenNoBucketOrFileId_ThrowsValidationException() {
    DerivativeDto noBucket = newDerivative(upload.getFileIdentifier());
    noBucket.setBucket(null);
    DerivativeDto noFileId = newDerivative(upload.getFileIdentifier());
    noFileId.setFileIdentifier(null);
    Assertions.assertThrows(ValidationException.class, () -> derivativeRepository.create(noBucket));
    Assertions.assertThrows(ValidationException.class, () -> derivativeRepository.create(noFileId));
  }

  @Test
  void create_WhenNoObjectUpload_ThrowsValidationException() {
    Assertions.assertThrows(
      ValidationException.class,
      () -> derivativeRepository.create(newDerivative(UUID.randomUUID())));
  }

  private static DerivativeDto newDerivative(UUID fileIdentifier) {
    DerivativeDto dto = new DerivativeDto();
    dto.setBucket("dina bucket");
    dto.setDcType(DcType.IMAGE);//TODO does user submit this?
    dto.setFileExtension(".something");//TODO does user submit this?
    dto.setFileIdentifier(fileIdentifier);
    return dto;
  }
}
