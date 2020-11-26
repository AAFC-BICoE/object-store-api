package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.exif.ExifParser;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectUploadServiceIT extends BaseIntegrationTest {

  @Inject
  private ObjectUploadService objectUploadService;

  @Test
  public void objectUploadService_OnCreate_ParseDateTimeDigitizedFromExif() {

    ObjectUpload testObjectUpload = ObjectUploadFactory.newObjectUpload()
        .exif(Map.of(ExifParser.DATE_TAKEN_POSSIBLE_TAGS.get(0), "2020:11:13 10:03:17"))
        .build();
    ObjectUpload testObjectUploadAfterCreate = objectUploadService.create(testObjectUpload);

    ObjectUpload reloadedObjectUpload = objectUploadService.findOne(testObjectUploadAfterCreate.getFileIdentifier(), ObjectUpload.class);
    assertEquals("2020-11-13T10:03:17", reloadedObjectUpload.getDateTimeDigitized());
  }

}
