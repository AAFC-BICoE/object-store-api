package ca.gc.aafc.objectstore.api.repository;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Inject;
import javax.persistence.criteria.Predicate;

import org.apache.tika.mime.MimeTypeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;

import ca.gc.aafc.dina.exception.ResourceNotFoundException;
import ca.gc.aafc.dina.jsonapi.JsonApiDocument;
import ca.gc.aafc.dina.jsonapi.JsonApiDocuments;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.config.AsyncOverrideConfig;
import ca.gc.aafc.objectstore.api.dto.DerivativeGenerationDto;
import ca.gc.aafc.objectstore.api.dto.ObjectUploadDto;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.file.FileController;
import ca.gc.aafc.objectstore.api.minio.MinioTestContainerInitializer;
import ca.gc.aafc.objectstore.api.storage.FileStorage;
import ca.gc.aafc.objectstore.api.testsupport.factories.DerivativeFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.MultipartFileFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ContextConfiguration(initializers = MinioTestContainerInitializer.class)
@Import(AsyncOverrideConfig.class)
public class DerivativeGenerationRepositoryIT extends BaseIntegrationTest {

  private static final String BUCKET_NAME = "derivative-generation-it-bucket";

  @Inject
  private ResourceLoader resourceLoader;

  @Inject
  private FileController fileController;

  @Inject
  private FileStorage fileStorage;

  @Inject
  private DerivativeGenerationRepository derivativeGenerationRepository;

  @Test
  public void derivativeGeneration_generatedFromDerivativeThumbnailMissing_generationSucceed()
    throws IOException, MimeTypeException, NoSuchAlgorithmException, ResourceNotFoundException {

    // setup original file (txt file)
    MockMultipartFile originalMultipart = MultipartFileFactory.createMockMultipartFile(
      resourceLoader, "testfile.txt", MediaType.TEXT_PLAIN_VALUE);
    ObjectUploadDto uploadResponse = fileController.handleFileUpload(originalMultipart, BUCKET_NAME);
    ObjectStoreMetadata acDerivedFrom = ObjectStoreMetadataFactory.newObjectStoreMetadata()
      .fileIdentifier(uploadResponse.getFileIdentifier()).build();
    objectStoreMetaDataService.create(acDerivedFrom);

    // setup derivative (jpg of the txt file)
    MockMultipartFile derivativeMultipart = MultipartFileFactory.createMockMultipartFile(
      resourceLoader, "testfile.jpg", MediaType.IMAGE_JPEG_VALUE);
    ObjectUploadDto derivativeUploadResponse = fileController.handleDerivativeUpload(derivativeMultipart, BUCKET_NAME);
    Derivative derivative = DerivativeFactory.newDerivative(acDerivedFrom, derivativeUploadResponse.getFileIdentifier()).build();
    derivativeService.create(derivative);

    // make sure the thumbnail exists
    Derivative thumbResult = findThumbnailByGeneratedFromDerivative(derivative)
      .orElseGet(() -> Assertions.fail("A derivative for a thumbnail should of been generated"));
    UUID firstThumbnailUUID = thumbResult.getUuid();

    // Scenario 1: the thumbnail entity exists but not the file
    // delete the file directly in the FileStorage but keep the entity
    fileStorage.deleteFile(thumbResult.getBucket(), thumbResult.getFilename(), true);

    JsonApiDocument docToCreate = JsonApiDocuments.createJsonApiDocument(
      null, DerivativeGenerationDto.TYPENAME,
      JsonAPITestHelper.toAttributeMap(DerivativeGenerationDto.builder()
        .metadataUuid(acDerivedFrom.getUuid())
        .derivativeType(Derivative.DerivativeType.THUMBNAIL_IMAGE)
        .derivedFromType(Derivative.DerivativeType.LARGE_IMAGE).build()));

    derivativeGenerationRepository.onCreate(docToCreate);

    assertTrue(fileStorage.getFileInfo(thumbResult.getBucket(), thumbResult.getFilename(), true).isPresent());

    // Scenario 2: thumbnail entity doesn't exist
    // delete the derivative entity
    derivativeService.delete(thumbResult);
    derivativeService.refresh(derivative);
    assertTrue(findThumbnailByGeneratedFromDerivative(derivative).isEmpty());


    docToCreate = JsonApiDocuments.createJsonApiDocument(
      null, DerivativeGenerationDto.TYPENAME,
      JsonAPITestHelper.toAttributeMap(DerivativeGenerationDto.builder()
      .metadataUuid(acDerivedFrom.getUuid())
      .derivativeType(Derivative.DerivativeType.THUMBNAIL_IMAGE)
      .derivedFromType(Derivative.DerivativeType.LARGE_IMAGE).build()));

      derivativeGenerationRepository.onCreate(docToCreate);

    Derivative thumbResult2 = findThumbnailByGeneratedFromDerivative(derivative)
      .orElseGet(() -> Assertions.fail("A derivative for a thumbnail should of been generated"));
    assertNotEquals(firstThumbnailUUID, thumbResult2.getUuid());
    assertTrue(fileStorage.getFileInfo(thumbResult2.getBucket(), thumbResult2.getFilename(), true).isPresent());
  }

  @Test
  public void derivativeGeneration_ThumbnailMissing_generationSucceed()
    throws IOException, MimeTypeException, NoSuchAlgorithmException, ResourceNotFoundException {

    MockMultipartFile multipartUpload = MultipartFileFactory.createMockMultipartFile(
      resourceLoader, "testfile.jpg", MediaType.IMAGE_JPEG_VALUE);
    ObjectUploadDto uploadResponse = fileController.handleFileUpload(multipartUpload, BUCKET_NAME);
    ObjectStoreMetadata osMetadata = ObjectStoreMetadataFactory.newObjectStoreMetadata()
      .bucket(BUCKET_NAME)
      .fileIdentifier(uploadResponse.getFileIdentifier()).build();
    objectStoreMetaDataService.create(osMetadata);

    // make sure the thumbnail exists
    Derivative thumbResult = derivativeGenerationService.findThumbnailDerivativeForMetadata(osMetadata)
      .orElseGet(() -> Assertions.fail("A derivative for a thumbnail should of been generated"));
    UUID firstThumbnailUUID = thumbResult.getUuid();

    // Scenario 1: the thumbnail entity exists but not the file
    // delete the file directly in the FileStorage but keep the entity
    fileStorage.deleteFile(thumbResult.getBucket(), thumbResult.getFilename(), true);

    JsonApiDocument docToCreate = JsonApiDocuments.createJsonApiDocument(
      null, DerivativeGenerationDto.TYPENAME,
      JsonAPITestHelper.toAttributeMap(DerivativeGenerationDto.builder()
        .metadataUuid(osMetadata.getUuid())
        .derivativeType(Derivative.DerivativeType.THUMBNAIL_IMAGE)
        .build()));
    derivativeGenerationRepository.onCreate(docToCreate);

    assertTrue(fileStorage.getFileInfo(thumbResult.getBucket(), thumbResult.getFilename(), true).isPresent());

    // Scenario 2: thumbnail entity doesn't exist
    // delete the derivative entity
    derivativeService.delete(thumbResult);
    derivativeService.refresh(osMetadata);
    assertTrue(derivativeGenerationService.findThumbnailDerivativeForMetadata(osMetadata).isEmpty());

    docToCreate = JsonApiDocuments.createJsonApiDocument(
      null, DerivativeGenerationDto.TYPENAME,
      JsonAPITestHelper.toAttributeMap(DerivativeGenerationDto.builder()
        .metadataUuid(osMetadata.getUuid())
        .derivativeType(Derivative.DerivativeType.THUMBNAIL_IMAGE)
        .build()));
    derivativeGenerationRepository.onCreate(docToCreate);

    Derivative thumbResult2 = derivativeGenerationService.findThumbnailDerivativeForMetadata(osMetadata)
      .orElseGet(() -> Assertions.fail("A derivative for a thumbnail should of been generated"));
    assertNotEquals(firstThumbnailUUID, thumbResult2.getUuid());
    assertTrue(fileStorage.getFileInfo(thumbResult2.getBucket(), thumbResult2.getFilename(), true).isPresent());
  }

  private Optional<Derivative> findThumbnailByGeneratedFromDerivative(Derivative derivative) {
    return derivativeService.findAll(
        Derivative.class, (criteriaBuilder, derivativeRoot) -> new Predicate[]{
          criteriaBuilder.equal(derivativeRoot.get(Derivative.GENERATED_FROM_DERIVATIVE_PROP), derivative),
        }, null, 0, 1)
      .stream().findFirst();
  }

}
