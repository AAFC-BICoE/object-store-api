package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.objectstore.api.OrphanRemovalConfiguration;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.minio.MinioFileService;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Log4j2
public class ObjectOrphanRemovalService {

  private static final int MAX_ORPHAN_QUERY_LIMIT = 1000;

  private final ObjectUploadService objectUploadService;
  private final MinioFileService fileService;
  private final BaseDAO baseDAO;

  private final OrphanRemovalConfiguration.OrphanRemovalExpirationSetting expiration;

  public ObjectOrphanRemovalService(
    ObjectUploadService objectUploadService,
    MinioFileService fileService,
    BaseDAO baseDAO,
    OrphanRemovalConfiguration orphanRemovalConfiguration
  ) {
    this.objectUploadService = objectUploadService;
    this.fileService = fileService;
    this.baseDAO = baseDAO;
    this.expiration = orphanRemovalConfiguration.getExpiration();
  }

  // Default cron expression '-' as default value will disable execution
  @Scheduled(cron = "${orphan-removal.cron.expression:-}")
  @Transactional
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
    String sql = 
      "SELECT ou " +
      "FROM " + ObjectUpload.class.getCanonicalName() + " ou " +
      "LEFT JOIN " + ObjectStoreMetadata.class.getCanonicalName() + " m ON ou.fileIdentifier = m.fileIdentifier " +
      "LEFT JOIN " + Derivative.class.getCanonicalName() + " d ON ou.fileIdentifier = d.fileIdentifier " +
      "WHERE m.fileIdentifier IS NULL AND d.fileIdentifier IS NULL";

    return baseDAO.resultListFromQuery(
        ObjectUpload.class,
        sql,
        0,
        MAX_ORPHAN_QUERY_LIMIT,
        null);
  }

  private boolean isExpired(ObjectUpload upload) {
    LocalDateTime uploadDate = upload.getCreatedOn().toLocalDateTime();
    LocalDateTime expiration;
    if (this.expiration == null || this.expiration.getObjectMaxAge() == null) {
      expiration = uploadDate.plusWeeks(1);
    } else {
      expiration = uploadDate.plusSeconds(this.expiration.getObjectMaxAge().getSeconds());
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
