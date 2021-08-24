package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.minio.MinioFileService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
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
    return objectUploadService.findAll(
      ObjectUpload.class,
      (criteriaBuilder, objectUploadRoot) -> new Predicate[]{},
      null,
      0,
      Integer.MAX_VALUE);
  }

  private boolean checkAge(ObjectUpload objectUpload) {
    return objectUpload.getCreatedOn()
      .toLocalDate()
      .isBefore(LocalDateTime.now().minusWeeks(2).toLocalDate());
  }

  private void deleteUpload(ObjectUpload objectUpload) {
    objectUploadService.delete(objectUpload);
  }

  @SneakyThrows
  private void deleteMinioFile(ObjectUpload objectUpload) {
    fileService.removeFile(
      objectUpload.getBucket(),
      objectUpload.getFileIdentifier() + objectUpload.getEvaluatedFileExtension());
  }

}
