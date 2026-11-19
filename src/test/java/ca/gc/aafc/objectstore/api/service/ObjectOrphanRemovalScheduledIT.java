package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.ObjectStoreApiLauncher;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.storage.FileManagement;
import ca.gc.aafc.objectstore.api.storage.FileStorage;
import ca.gc.aafc.objectstore.api.storage.VersityWGTestContainerInitializer;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import jakarta.inject.Inject;
import jakarta.persistence.criteria.Predicate;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Kept separate from {@link ObjectOrphanRemovalServiceIT} so the scheduler only runs in a context
 * where no other test is creating or deleting uploads.
 */
// Close the context afterwards so its scheduler doesn't keep deleting uploads during other test classes
@DirtiesContext
@ContextConfiguration(initializers = VersityWGTestContainerInitializer.class)
@SpringBootTest(classes = ObjectStoreApiLauncher.class, properties = {
  "orphan-removal.expiration.object_max_age=12d",
  "orphan-removal.cron.expression=*/1 * * * * *",
  "dev-user.enabled=true"
})
class ObjectOrphanRemovalScheduledIT extends BaseIntegrationTest {

  private static final String BUCKET = "objectuploadbucket";
  private static final String INTERVAL_2_WEEKS =
    "UPDATE object_upload SET created_on = created_on - interval '2 weeks'";

  @Inject
  private FileManagement fileManagement;

  @Inject
  private FileStorage fileStorage;

  @BeforeEach
  void setUp() throws IOException {
    fileManagement.ensureBucketExists(BUCKET);
  }

  @SneakyThrows
  @Test
  void removeOrphans_ScheduledAction() {
    persistOrphanRecord();
    ObjectUpload upload = findUploads().getFirst();
    String fileName = storeFileForUpload(upload);

    Thread.sleep(2000);

    assertTrue(
      fileStorage.retrieveFile(BUCKET, fileName, false).isEmpty(),
      "There should be no returned files");
    assertTrue(findUploads().isEmpty(), "There should be no upload record");
  }

  private List<ObjectUpload> findUploads() {
    return objectUploadService.findAll(ObjectUpload.class,
      (criteriaBuilder, objectUploadRoot) -> new Predicate[] {
        criteriaBuilder.equal(objectUploadRoot.get("bucket"), BUCKET)
      }, null, 0, 20);
  }

  private void persistOrphanRecord() {
    service.runInNewTransaction(em -> {
      ObjectUpload upload = ObjectUploadFactory.newObjectUpload().build();
      upload.setBucket(BUCKET);
      em.persist(upload);
      em.createNativeQuery(INTERVAL_2_WEEKS).executeUpdate();
    });
  }

  @SneakyThrows
  private String storeFileForUpload(ObjectUpload upload) {
    String fileName = upload.getCompleteFileName();
    fileStorage.storeFile(
      BUCKET,
      fileName,
      upload.getIsDerivative(),
      upload.getEvaluatedMediaType(),
      new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8))
    );
    return fileName;
  }
}
