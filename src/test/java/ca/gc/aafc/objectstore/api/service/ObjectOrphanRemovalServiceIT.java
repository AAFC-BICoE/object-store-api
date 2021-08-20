package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.exif.ExifParser;
import ca.gc.aafc.objectstore.api.minio.MinioFileService;
import ca.gc.aafc.objectstore.api.minio.MinioTestContainerInitializer;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(initializers = MinioTestContainerInitializer.class)
class ObjectOrphanRemovalServiceIT extends BaseIntegrationTest {

  @Inject
  private ObjectOrphanRemovalService serviceUnderTest;

  @Inject
  private ObjectUploadService objectUploadService;

  @Inject
  private MinioFileService fileService;

  @Test
  void removeOrphans() {
    ObjectUpload upload = objectUploadService.create( ObjectUploadFactory.newObjectUpload()
      .exif(Map.of(ExifParser.DATE_TAKEN_POSSIBLE_TAGS.get(0), "2020:11:13 10:03:17"))
      .build());




  }

}