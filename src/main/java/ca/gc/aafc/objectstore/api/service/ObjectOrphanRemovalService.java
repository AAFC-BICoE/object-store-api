package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.minio.MinioFileService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Subquery;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class ObjectOrphanRemovalService {

  private static final String FILE_IDENTIFIER_KEY = "fileIdentifier";
  private final ObjectUploadService objectUploadService;
  private final MinioFileService fileService;

  public void removeObjectOrphans() {
    List<ObjectUpload> orphans = findOrphans();
    orphans.forEach(objectUpload -> {
      if (isAgeOlderThenTwoWeeks(objectUpload)) {
        deleteUpload(objectUpload);
        deleteMinioFile(objectUpload);
      }
    });
  }

  private List<ObjectUpload> findOrphans() {
    return objectUploadService.findAll(
      ObjectUpload.class,
      (criteriaBuilder, objectUploadRoot) -> {
        Subquery<UUID> metaSubQuery = criteriaBuilder.createQuery(ObjectUpload.class).subquery(UUID.class);
        Subquery<UUID> derivSubQuery = criteriaBuilder.createQuery(Derivative.class).subquery(UUID.class);
        return new Predicate[]{
          criteriaBuilder.in(objectUploadRoot.get(FILE_IDENTIFIER_KEY)).value(
            metaSubQuery.select(metaSubQuery.from(ObjectStoreMetadata.class).get(FILE_IDENTIFIER_KEY))).not(),
          criteriaBuilder.in(objectUploadRoot.get(FILE_IDENTIFIER_KEY)).value(
            derivSubQuery.select(derivSubQuery.from(Derivative.class).get(FILE_IDENTIFIER_KEY))).not()};
      }, null, 0, Integer.MAX_VALUE);
  }

  private boolean isAgeOlderThenTwoWeeks(ObjectUpload upload) {
    return upload.getCreatedOn().toLocalDate().isBefore(LocalDateTime.now().minusWeeks(2).toLocalDate());
  }

  private void deleteUpload(ObjectUpload objectUpload) {
    objectUploadService.delete(objectUpload);
    log.info("object upload with file identifier: "
      + objectUpload.getFileIdentifier().toString() + " has been removed");
  }

  @SneakyThrows
  private void deleteMinioFile(ObjectUpload objectUpload) {
    String fileName = objectUpload.getFileIdentifier() + objectUpload.getEvaluatedFileExtension();
    fileService.removeFile(
      objectUpload.getBucket(),
      fileName,
      objectUpload.getIsDerivative());
    log.info(fileName + " removed from minio");
  }

}
