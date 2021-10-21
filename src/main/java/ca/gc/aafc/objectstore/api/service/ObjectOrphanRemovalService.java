package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.objectstore.api.OrphanRemovalConfiguration;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.minio.MinioFileService;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Subquery;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Log4j2
public class ObjectOrphanRemovalService {

  private static final String FILE_IDENTIFIER_KEY = "fileIdentifier";
  private final ObjectUploadService objectUploadService;
  private final MinioFileService fileService;
  private final OrphanRemovalConfiguration.OrphanRemovalExpirationSetting expiration;

  public ObjectOrphanRemovalService(
    ObjectUploadService objectUploadService,
    MinioFileService fileService,
    OrphanRemovalConfiguration orphanRemovalConfiguration
  ) {
    this.objectUploadService = objectUploadService;
    this.fileService = fileService;
    this.expiration = orphanRemovalConfiguration.getExpiration();
  }

  public void removeObjectOrphans() {
    List<ObjectUpload> orphans = findOrphans();
    orphans.forEach(objectUpload -> {
      if (isExpired(objectUpload)) {
        deleteUpload(objectUpload);
        deleteMinioFile(objectUpload);
      }
    });
  }

  /**
   * Returns all Object Uploads that have no matching file identifiers associated with Derivatives or
   * ObjectStoreMetadata. ObjectUpload.fileIdentifier NOT IN Derivatives.fileIdentifier AND
   * ObjectStoreMetadata.fileIdentifier.
   *
   * @return all Object Uploads that have no matching file identifiers associated with Derivatives or
   * ObjectStoreMetadata.
   */
  private List<ObjectUpload> findOrphans() {
    return objectUploadService.findAll(
      ObjectUpload.class,
      (criteriaBuilder, objectUploadRoot) -> {
        Subquery<UUID> metaSubQuery = criteriaBuilder.createQuery(ObjectStoreMetadata.class)
          .subquery(UUID.class);
        Subquery<UUID> derivSubQuery = criteriaBuilder.createQuery(Derivative.class).subquery(UUID.class);
        return new Predicate[]{
          criteriaBuilder.in(objectUploadRoot.get(FILE_IDENTIFIER_KEY)).value(
            metaSubQuery.select(metaSubQuery.from(ObjectStoreMetadata.class).get(FILE_IDENTIFIER_KEY))).not(),
          criteriaBuilder.in(objectUploadRoot.get(FILE_IDENTIFIER_KEY)).value(
            derivSubQuery.select(derivSubQuery.from(Derivative.class).get(FILE_IDENTIFIER_KEY))).not()};
      }, null, 0, Integer.MAX_VALUE);
  }

  private boolean isExpired(ObjectUpload upload) {
    LocalDateTime uploadDate = upload.getCreatedOn().toLocalDateTime();
    LocalDateTime expiration;
    if (this.expiration == null) {
      expiration = uploadDate.plusWeeks(1);
    } else {
      expiration = uploadDate
        .plusSeconds(this.expiration.getSeconds())
        .plusMinutes(this.expiration.getMinutes())
        .plusHours(this.expiration.getHours())
        .plusDays(this.expiration.getDays())
        .plusWeeks(this.expiration.getWeeks());
    }
    return LocalDateTime.now().isAfter(expiration);
  }

  private void deleteUpload(ObjectUpload objectUpload) {
    objectUploadService.delete(objectUpload);
    log.info("object upload with file identifier: "
      + objectUpload.getFileIdentifier().toString() + " has been removed");
  }

  @SneakyThrows
  private void deleteMinioFile(ObjectUpload objectUpload) {
    String fileName = objectUpload.getCompleteFileName();
    fileService.removeFile(
      objectUpload.getBucket(),
      fileName,
      objectUpload.getIsDerivative());
    log.info(fileName + " removed from minio");
  }

}
