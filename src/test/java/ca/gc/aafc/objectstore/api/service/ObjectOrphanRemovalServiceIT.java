package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.minio.MinioFileService;
import ca.gc.aafc.objectstore.api.minio.MinioTestContainerInitializer;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@ContextConfiguration(initializers = MinioTestContainerInitializer.class)
class ObjectOrphanRemovalServiceIT extends BaseIntegrationTest {

  public static final String BUCKET = "bucket";

  @Inject
  private ObjectOrphanRemovalService serviceUnderTest;

  @Inject
  private ObjectUploadService objectUploadService;

  @Inject
  private MinioFileService fileService;

  @SneakyThrows
  @Test
  void removeOrphans_WhenOrphan_OrphanRemoved() {
    fileService.ensureBucketExists(BUCKET);

    ObjectUpload upload = ObjectUploadFactory.newObjectUpload().build();
    upload.setBucket(BUCKET);
    upload = objectUploadService.create(upload);

    String fileName = upload.getFileIdentifier().toString() + upload.getEvaluatedFileExtension();
    fileService.storeFile(
      fileName,
      new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)),
      upload.getEvaluatedMediaType(),
      BUCKET,
      upload.getIsDerivative());


    // Mock LocalDateTime to simulate the passage of time.
    LocalDateTime future = LocalDateTime.now().plusYears(3);
    try (MockedStatic<LocalDateTime> mocked = Mockito.mockStatic(LocalDateTime.class)) {
      mocked.when(LocalDateTime::now).thenReturn(future);

      serviceUnderTest.removeObjectOrphans(); // method under test

      Assertions.assertTrue(
        fileService.getFile(fileName, BUCKET, false).isEmpty(),
        "There should be no returned files");
      Assertions.assertNull(objectUploadService.findOne(upload.getFileIdentifier(), ObjectUpload.class),
        "There should be no upload record");
    }
  }

  @Test
  void removeOrphans_WhenAgeLessThen() {

  }

  @Test
  void removeOrphans_WhenLinkedToMetadata() {

  }

  @Test
  void removeOrphans_WhenLinkedToDerivative() {

  }

}
