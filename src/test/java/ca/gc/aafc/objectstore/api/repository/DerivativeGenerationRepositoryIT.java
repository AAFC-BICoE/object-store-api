package ca.gc.aafc.objectstore.api.repository;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
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
  public void generateThumbnail() throws IOException, MimeTypeException, NoSuchAlgorithmException {

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
    Derivative thumbResult = findThumbnailOrThrow(derivative);
    // delete the file directly in the FileStorage but keep the entity
    fileStorage.deleteFile(thumbResult.getBucket(), thumbResult.getFilename(), true);
   // derivativeService.delete(thumbResult);

    var dto = derivativeGenerationRepository.create(DerivativeGenerationDto.builder()
      .metadataUuid(acDerivedFrom.getUuid())
      .derivativeType(Derivative.DerivativeType.THUMBNAIL_IMAGE)
      .derivedFromType(Derivative.DerivativeType.LARGE_IMAGE).build());

    assertTrue(fileStorage.getFileInfo(thumbResult.getBucket(), thumbResult.getFilename(), true).isPresent());

   // thumbResult = findThumbnailOrThrow(derivative);

  }

  private Derivative findThumbnailOrThrow(Derivative derivative) {
    return derivativeService.findAll(
        Derivative.class, (criteriaBuilder, derivativeRoot) -> new Predicate[]{
          criteriaBuilder.equal(derivativeRoot.get("generatedFromDerivative"), derivative),
        }, null, 0, 1)
      .stream().findFirst()
      .orElseGet(() -> Assertions.fail("A derivative for a thumbnail should of been generated"));
  }

}
