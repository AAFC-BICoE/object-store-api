package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.minio.MinioFileService;
import ca.gc.aafc.objectstore.api.minio.MinioTestContainerInitializer;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import javax.persistence.criteria.Predicate;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@ContextConfiguration(initializers = MinioTestContainerInitializer.class)
class ObjectOrphanRemovalServiceIT extends BaseIntegrationTest {

  public static final String BUCKET = "bucket";

  @Inject
  private ObjectOrphanRemovalService serviceUnderTest;

  @Inject
  private ObjectStoreMetaDataService metaDataService;

  @Inject
  private ObjectUploadService objectUploadService;

  @Inject
  private MinioFileService fileService;

  @BeforeEach
  void setUp() throws IOException {
    fileService.ensureBucketExists(BUCKET);
  }

  @SneakyThrows
  @Test
  void removeOrphans_WhenOrphanAndOlderThen2Weeks_OrphanRemoved() {
    service.runInNewTransaction(em -> {
      ObjectUpload upload = ObjectUploadFactory.newObjectUpload().build();
      upload.setBucket(BUCKET);
      em.persist(upload);
      em.createNativeQuery("UPDATE object_upload SET created_on = created_on - interval '3 weeks'")
        .executeUpdate(); // Mock record created in the past and orphan
    });

    ObjectUpload upload = objectUploadService.findAll(ObjectUpload.class,
      (criteriaBuilder, objectUploadRoot) -> new Predicate[]{}, null, 0, 10).get(0);

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
  void removeOrphans_WhenLinkedToMetadata() {
    service.runInNewTransaction(em -> {
      ObjectUpload upload = ObjectUploadFactory.newObjectUpload().build();
      upload.setBucket(BUCKET);
      em.persist(upload);
      em.createNativeQuery("UPDATE object_upload SET created_on = created_on - interval '3 weeks'")
        .executeUpdate(); // Mock record created in the past
    });

    ObjectUpload upload = objectUploadService.findAll(ObjectUpload.class,
      (criteriaBuilder, objectUploadRoot) -> new Predicate[]{}, null, 0, 10).get(0);

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

  @Test
  void removeOrphans_WhenLinkedToDerivative() {

  }

  @SneakyThrows
  private String storeFileForUpload(ObjectUpload upload) {
    String fileName = upload.getFileIdentifier().toString() + upload.getEvaluatedFileExtension();
    fileService.storeFile(
      fileName,
      new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)),
      upload.getEvaluatedMediaType(),
      BUCKET,
      upload.getIsDerivative());
    return fileName;
  }
}
