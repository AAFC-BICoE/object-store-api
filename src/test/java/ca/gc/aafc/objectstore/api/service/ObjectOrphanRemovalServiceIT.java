package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.ObjectStoreApiLauncher;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.minio.MinioFileService;
import ca.gc.aafc.objectstore.api.minio.MinioTestContainerInitializer;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import javax.persistence.criteria.Predicate;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@ContextConfiguration(initializers = MinioTestContainerInitializer.class)
@SpringBootTest(classes = ObjectStoreApiLauncher.class, properties = "orphan-removal.expiration.duration=12d")
class ObjectOrphanRemovalServiceIT extends BaseIntegrationTest {

  public static final String BUCKET = "bucket";
  public static final String INTERVAL_2_WEEKS = "UPDATE object_upload SET created_on = created_on - interval '2 weeks'";

  @Inject
  private ObjectOrphanRemovalService serviceUnderTest;

  @Inject
  private ObjectStoreMetaDataService metaDataService;

  @Inject
  private ObjectUploadService objectUploadService;

  @Inject
  private DerivativeService derivativeService;

  @Inject
  private MinioFileService fileService;

  @BeforeEach
  void setUp() throws IOException {
    fileService.ensureBucketExists(BUCKET);
    findUploads().forEach(objectUpload -> objectUploadService.delete(objectUpload));
  }

  @AfterEach
  void tearDown() {
    // Test clean up
    findUploads().forEach(objectUpload -> objectUploadService.delete(objectUpload));
  }

  @SneakyThrows
  @Test
  void removeOrphans_WhenOrphanAndOlderThen2Weeks_OrphanRemoved() {
    persistOrphanRecord();
    ObjectUpload upload = findUploads().get(0);
    String fileName = storeFileForUpload(upload);

    serviceUnderTest.removeObjectOrphans(); // method under test

    Assertions.assertTrue(
      fileService.getFile(fileName, BUCKET, false).isEmpty(),
      "There should be no returned files");
    Assertions.assertNull(
      objectUploadService.findOne(upload.getFileIdentifier(), ObjectUpload.class),
      "There should be no upload record");
  }

  @SneakyThrows
  @Test
  void removeOrphans_WhenOrphanAndAgeLessThen_OrphanNotRemoved() {
    ObjectUpload upload = ObjectUploadFactory.newObjectUpload().build();
    upload.setBucket(BUCKET);
    upload = objectUploadService.create(upload); // record will have current date and is orphan
    String fileName = storeFileForUpload(upload);

    serviceUnderTest.removeObjectOrphans(); // method under test

    Assertions.assertTrue(
      fileService.getFile(fileName, BUCKET, false).isPresent(),
      "There should be a returned file");
    Assertions.assertNotNull(
      objectUploadService.findOne(upload.getFileIdentifier(), ObjectUpload.class),
      "There should be a upload record");
  }

  @SneakyThrows
  @Test
  void removeOrphans_WhenLinkedToMetadata_OrphanNotRemoved() {
    persistOrphanRecord();
    ObjectUpload upload = findUploads().get(0);
    String fileName = storeFileForUpload(upload);

    metaDataService.create(ObjectStoreMetadataFactory.newObjectStoreMetadata()
      .fileIdentifier(upload.getFileIdentifier())
      .build());

    serviceUnderTest.removeObjectOrphans(); // method under test

    Assertions.assertTrue(
      fileService.getFile(fileName, BUCKET, false).isPresent(),
      "There should be a returned file");
    Assertions.assertNotNull(
      objectUploadService.findOne(upload.getFileIdentifier(), ObjectUpload.class),
      "There should be a upload record");
  }

  @SneakyThrows
  @Test
  void removeOrphans_WhenLinkedToDerivative_OrphanNotRemoved() {
    ObjectUpload acDerivedRecord = objectUploadService.create(
      ObjectUploadFactory.newObjectUpload().bucket(BUCKET).build());
    ObjectStoreMetadata acDerivedFrom = metaDataService.create(
      ObjectStoreMetadataFactory.newObjectStoreMetadata().fileIdentifier(acDerivedRecord.getFileIdentifier())
        .build());
    persistOrphanDerivative();

    ObjectUpload derivativeUpload = findUploads().stream().filter(ObjectUpload::getIsDerivative).findFirst()
      .orElseGet(() -> {
        Assertions.fail("a derivative record should of been generated");
        return null;
      });

    String fileName = storeFileForUpload(derivativeUpload);

    derivativeService.create(Derivative.builder()
      .fileIdentifier(derivativeUpload.getFileIdentifier())
      .fileExtension(derivativeUpload.getEvaluatedFileExtension())
      .bucket(derivativeUpload.getBucket())
      .acDerivedFrom(acDerivedFrom)
      .createdBy(derivativeUpload.getCreatedBy())
      .acHashValue("abc")
      .dcType(DcType.TEXT)
      .dcFormat(MediaType.TEXT_PLAIN_VALUE)
      .build());

    serviceUnderTest.removeObjectOrphans(); // method under test

    Assertions.assertTrue(
      fileService.getFile(fileName, BUCKET, true).isPresent(),
      "There should be a returned file");
    Assertions.assertNotNull(
      objectUploadService.findOne(derivativeUpload.getFileIdentifier(), ObjectUpload.class),
      "There should be a derivativeUpload record");
  }

  @SneakyThrows
  @Test
  void removeOrphans_WhenOrphanFileIsDerivativeFile_OrphanRemoved() {
    persistOrphanDerivative();
    ObjectUpload derivativeUpload = findUploads().stream().filter(ObjectUpload::getIsDerivative).findFirst()
      .orElseGet(() -> {
        Assertions.fail("a derivative record should of been generated");
        return null;
      });
    String fileName = storeFileForUpload(derivativeUpload);

    serviceUnderTest.removeObjectOrphans(); // method under test

    Assertions.assertTrue(
      fileService.getFile(fileName, BUCKET, true).isEmpty(),
      "There should be no returned files");
    Assertions.assertNull(
      objectUploadService.findOne(derivativeUpload.getFileIdentifier(), ObjectUpload.class),
      "There should be no upload record");
  }

  private List<ObjectUpload> findUploads() {
    return objectUploadService.findAll(ObjectUpload.class,
      (criteriaBuilder, objectUploadRoot) -> new Predicate[]{}, null, 0, 20);
  }

  private void persistOrphanRecord() {
    service.runInNewTransaction(em -> {
      ObjectUpload upload = ObjectUploadFactory.newObjectUpload().build();
      upload.setBucket(BUCKET);
      em.persist(upload);
      em.createNativeQuery(INTERVAL_2_WEEKS).executeUpdate(); // Mock record created in the past and orphan
    });
  }

  private void persistOrphanDerivative() {
    service.runInNewTransaction(em -> {
      ObjectUpload upload = ObjectUploadFactory.newObjectUpload().build();
      upload.setIsDerivative(true);
      upload.setBucket(BUCKET);
      em.persist(upload);
      em.createNativeQuery(INTERVAL_2_WEEKS).executeUpdate(); // Mock record created in the past
    });
  }

  @SneakyThrows
  private String storeFileForUpload(ObjectUpload upload) {
    String fileName = upload.getCompleteFileName();
    fileService.storeFile(
      fileName,
      new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)),
      upload.getEvaluatedMediaType(),
      BUCKET,
      upload.getIsDerivative());
    return fileName;
  }
}
