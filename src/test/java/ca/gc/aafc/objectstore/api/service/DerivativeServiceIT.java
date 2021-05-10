package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.MinioTestConfiguration;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.file.FileObjectInfo;
import ca.gc.aafc.objectstore.api.file.ThumbnailGenerator;
import ca.gc.aafc.objectstore.api.minio.MinioFileService;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.MediaType;

import javax.persistence.criteria.Predicate;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Import({MinioTestConfiguration.class, DerivativeServiceIT.DerivativeServiceItConfig.class})
public class DerivativeServiceIT extends BaseIntegrationTest {

  private ObjectStoreMetadata acDerivedFrom;

  @MockBean
  private MinioFileService fileService;

  private final Resource drawing = new ClassPathResource("drawing.png");

  @BeforeEach
  void setUp() {
    acDerivedFrom = ObjectStoreMetadataFactory.newObjectStoreMetadata().build();
    this.service.save(acDerivedFrom);
    setMocks();
  }

  /**
   * We mock the file service to send the drawing.png so the Thumbnail builder library will run successfully.
   */
  @SneakyThrows
  private void setMocks() {
    InputStream file = drawing.getInputStream();
    InputStream copy = copyStream(file);
    file.close();

    Mockito.when(fileService.getFile(
      ArgumentMatchers.anyString(),
      ArgumentMatchers.anyString(),
      ArgumentMatchers.anyBoolean())
    ).thenReturn(Optional.of(copy));
    Mockito.when(fileService.getFileInfo(
      ArgumentMatchers.anyString(),
      ArgumentMatchers.anyString(),
      ArgumentMatchers.anyBoolean()
    )).thenReturn(Optional.of(FileObjectInfo.builder().build()));
  }

  @SneakyThrows
  private InputStream copyStream(InputStream file) {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    file.transferTo(byteArrayOutputStream);
    return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
  }

  @Test
  void create_WhenThumbnailSupported_ThumbnailGenerated() {
    Derivative derivative = newDerivative(acDerivedFrom);
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
  void create_WhenDerivativeIsThumbNail_ThumbNailNotGenerated() {
    Derivative derivative = newDerivative(acDerivedFrom);
    derivative.setDerivativeType(Derivative.DerivativeType.THUMBNAIL_IMAGE);
    derivativeService.create(derivative);
    Assertions.assertEquals(0, findAllByDerivative(derivative).size());
  }

  @Test
  void create_WhenThumbNailAlreadyExists_ThumbNailNotGenerated() {
    Derivative derivative = newDerivative(acDerivedFrom);
    derivativeService.create(derivative);

    ObjectUpload upload = ObjectUploadFactory.newObjectUpload()
      .fileIdentifier(UUID.randomUUID())
      .bucket(MinioTestConfiguration.TEST_BUCKET).evaluatedMediaType(MediaType.IMAGE_JPEG_VALUE).build();
    this.service.save(upload);
    Derivative derivative2 = newDerivative(acDerivedFrom);
    derivative2.setFileIdentifier(upload.getFileIdentifier());
    derivativeService.create(derivative2);

    Assertions.assertEquals(1, findAllByDerivative(derivative).size());
    Assertions.assertEquals(0, findAllByDerivative(derivative2).size());
  }

  @Test
  void generateThumbnail_DerivedFromMetaData_DerivativeGenerated() {
    derivativeService.generateThumbnail(
      MinioTestConfiguration.TEST_BUCKET,
      MinioTestConfiguration.TEST_FILE_IDENTIFIER + ".jpg",
      acDerivedFrom.getUuid(),
      MediaType.IMAGE_JPEG_VALUE,
      null,
      true);

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
        true));
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
        true));
  }

  private Derivative newDerivative(ObjectStoreMetadata child) {
    return Derivative.builder()
      .uuid(UUID.randomUUID())
      .fileIdentifier(UUID.randomUUID())
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

  @Configuration
  static class DerivativeServiceItConfig {
    @Bean
    @Primary
    public TaskExecutor taskExecutor() {
      return new SyncTaskExecutor();
    }
  }

}
