package ca.gc.aafc.objectstore.api.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.messaging.message.ReportTemplateUploadNotification;
import ca.gc.aafc.dina.util.UUIDHelper;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.file.TemporaryObjectAccessController;
import ca.gc.aafc.dina.messaging.producer.DinaMessageProducer;
import ca.gc.aafc.objectstore.api.storage.FileStorage;

@Log4j2
@Service
public class ReportTemplateUploadService {

  private final FileStorage fileStorage;
  private final ObjectUploadService objectUploadService;
  private final TemporaryObjectAccessController toaCtrl;
  private final DinaMessageProducer dinaMessageProducer;

  public ReportTemplateUploadService(FileStorage fileStorage,
                                     ObjectUploadService objectUploadService,
                                     TemporaryObjectAccessController toaCtrl,
                                     DinaMessageProducer dinaMessageProducer) {
    this.fileStorage = fileStorage;
    this.objectUploadService = objectUploadService;
    this.toaCtrl = toaCtrl;
    this.dinaMessageProducer = dinaMessageProducer;
  }

  public ObjectUpload findOneObjectUpload(UUID fileIdentifier) {
    return objectUploadService.findOne(fileIdentifier, ObjectUpload.class);
  }

  /**
   * This function assumes permissions are already checked and that the mime-type is also
   * accepted.
   * @param fileIdentifier uuid of the ObjectUpload.
   * @return new generated template uuid
   */
  public ReportTemplateUploadResult handleTemplateUpload(UUID fileIdentifier) throws IOException {

    ObjectUpload objectUpload = findOneObjectUpload(fileIdentifier);
    UUID reportTemplateUUID = UUIDHelper.generateUUIDv7();

    String filename = reportTemplateUUID + objectUpload.getEvaluatedFileExtension();
    Path reportTemplateFilePath = toaCtrl.generatePath(filename);

    // copy
    Optional<InputStream> optIs =
      fileStorage.retrieveFile(objectUpload.getBucket(), objectUpload.getCompleteFileName(), objectUpload.getIsDerivative());
    try (InputStream is = optIs.orElseThrow(() -> new IllegalStateException("No InputStream available"));
         OutputStream os = Files.newOutputStream(reportTemplateFilePath)) {
      IOUtils.copy(is, os);
    }

    String toaKey = toaCtrl.registerObject(filename);

    ReportTemplateUploadNotification reportTemplateUploadNotification =
      ReportTemplateUploadNotification.builder()
        .uuid(reportTemplateUUID)
        .username(objectUpload.getCreatedBy())
        .toa(toaKey)
        .build();
    dinaMessageProducer.send(reportTemplateUploadNotification);

    return new ReportTemplateUploadResult(reportTemplateUUID, toaKey);
  }

  public record ReportTemplateUploadResult(UUID uuid, String toaKey) {
  }
}
