package ca.gc.aafc.objectstore.api.repository;

import ca.gc.aafc.dina.exception.ResourceGoneException;
import ca.gc.aafc.dina.exception.ResourceNotFoundException;
import ca.gc.aafc.dina.jsonapi.JsonApiDocument;
import ca.gc.aafc.dina.jsonapi.JsonApiDocuments;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.dina.util.UUIDHelper;
import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.dto.DerivativeDto;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;
import ca.gc.aafc.objectstore.api.testsupport.fixtures.DerivativeTestFixture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;
import javax.inject.Inject;
import javax.validation.ValidationException;
import java.util.UUID;

import static ca.gc.aafc.objectstore.api.repository.ObjectStoreModuleBaseRepositoryIT.dtoToJsonApiDocument;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
  void create() throws ResourceGoneException, ResourceNotFoundException {

    DerivativeDto dto = newDerivative(uploadTest_1.getFileIdentifier());
    JsonApiDocument docToCreate = DerivativeTestFixture.newJsonApiDocument(dto);

    DerivativeDto resource = derivativeRepository.create(docToCreate, null).getDto();
    DerivativeDto result = derivativeRepository.getOne(resource.getUuid(), "").getDto();

    assertEquals(resource.getDcType(), result.getDcType());
    assertEquals(resource.getFileIdentifier(), result.getFileIdentifier());
    assertEquals(resource.getDerivativeType(), result.getDerivativeType());
    assertEquals(resource.getDcFormat(), result.getDcFormat());
    assertEquals(uploadTest_1.getEvaluatedMediaType(), result.getDcFormat());

    // Auto generated fields
    assertNotNull(result.getBucket());
    assertNotNull(result.getFileExtension());
    assertNotNull(result.getAcHashValue());
    assertNotNull(result.getAcHashFunction());
    assertNotNull(result.getCreatedBy());
  }

  @Test
  void create_WhenNoFileId_ThrowsValidationException() {
    DerivativeDto dto = newDerivative(uploadTest_1.getFileIdentifier());
    dto.setFileIdentifier(null);
    JsonApiDocument docToCreate = DerivativeTestFixture.newJsonApiDocument(dto);
    assertThrows(ValidationException.class, () -> derivativeRepository.onCreate(docToCreate));
  }

  @Test
  void create_WhenNoObjectUpload_ThrowsValidationException() {

    JsonApiDocument docToCreate = dtoToJsonApiDocument(newDerivative(UUIDHelper.generateUUIDv7()));
    assertThrows(
      ValidationException.class,
      () -> derivativeRepository.onCreate(docToCreate));
  }

  @Test
  void create_WhenNotDerivative_ThrowsBadRequest() {
    ObjectUpload notDerivative = ObjectUploadFactory.newObjectUpload().build();
    notDerivative.setIsDerivative(false);
    objectUploadService.create(notDerivative);

    JsonApiDocument docToCreate = dtoToJsonApiDocument(newDerivative(notDerivative.getFileIdentifier()));
    assertThrows(
      ValidationException.class,
      () -> derivativeRepository.onCreate(docToCreate));
  }

  @Test
  void save() throws ResourceGoneException, ResourceNotFoundException {
    DerivativeDto dto = newDerivative(uploadTest_1.getFileIdentifier());
    dto.setPubliclyReleasable(false);

    JsonApiDocument docToCreate = DerivativeTestFixture.newJsonApiDocument(dto);

    DerivativeDto resource = derivativeRepository.create(docToCreate, null).getDto();
    DerivativeDto result = derivativeRepository.getOne(resource.getUuid(),"").getDto();
    assertFalse(result.getPubliclyReleasable());

    resource.setPubliclyReleasable(true);

    JsonApiDocument docToUpdate = dtoToJsonApiDocument(resource);
    derivativeRepository.update(docToUpdate);

    result = derivativeRepository.getOne(resource.getUuid(),"").getDto();
    assertTrue(result.getPubliclyReleasable());
    assertNotNull(result.getAcTags());
  }

  private DerivativeDto newDerivative(UUID fileIdentifier) {
    ObjectStoreMetadataDto from = new ObjectStoreMetadataDto();
    from.setUuid(acDerivedFrom.getUuid());

    DerivativeDto dto = DerivativeTestFixture.newDerivative(fileIdentifier);
    dto.setAcDerivedFrom(from);
    return dto;
  }
}
