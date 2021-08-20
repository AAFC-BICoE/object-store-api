package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.minio.MinioFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ObjectOrphanRemovalService {

  private final ObjectUploadService objectUploadService;
  private final MinioFileService fileService;

  public void removeObjectOrphans() {
    List<ObjectUpload> orphans = findOrphans();
    orphans.forEach(objectUpload -> {
      if (checkAge(objectUpload)) {
        deleteUpload(objectUpload);
        deleteMinioFile(objectUpload);
      }
    });
  }

  private List<ObjectUpload> findOrphans() {
    return Collections.emptyList();
  }

  private boolean checkAge(ObjectUpload objectUpload) {
    return false;
  }

  private void deleteUpload(ObjectUpload objectUpload) {

  }

  private void deleteMinioFile(ObjectUpload objectUpload) {

  }

}
