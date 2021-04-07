package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DefaultDinaService;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.file.ThumbnailService;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.Optional;
import java.util.UUID;

@Service
public class DerivativeService extends DefaultDinaService<Derivative> {
  private final ThumbnailService thumbnailService;

  public DerivativeService(
    @NonNull BaseDAO baseDAO,
    ThumbnailService thumbnailService
  ) {
    super(baseDAO);
    this.thumbnailService = thumbnailService;
  }

  @Override
  protected void preCreate(Derivative entity) {
    entity.setUuid(UUID.randomUUID());
    establishBiDirectionalAssociation(entity);
  }

  @Override
  protected void preUpdate(Derivative entity) {
    establishBiDirectionalAssociation(entity);
  }

  private static void establishBiDirectionalAssociation(Derivative entity) {
    if (entity.getAcDerivedFrom() != null) {
      entity.getAcDerivedFrom().addDerivative(entity);
    }
  }

  public Optional<Derivative> findByFileId(UUID fileId) {
    return findAll(Derivative.class,
      (cb, root) -> new Predicate[]{cb.equal(root.get("fileIdentifier"), fileId)},
      null, 0, 1)
      .stream().findFirst();
  }

  private void handleThumbNailGeneration(@NonNull Derivative resource, @NonNull String evaluatedMediaType) {
    String bucket = resource.getBucket();
    UUID derivedId = resource.getAcDerivedFrom().getUuid();
    String sourceFilename = resource.getFileIdentifier() + resource.getFileExtension();
    this.generateThumbnail(evaluatedMediaType, bucket, derivedId, sourceFilename);
  }

  public void generateThumbnail(
    String evaluatedMediaType,
    String bucket,
    UUID derivedId,
    String sourceFilename
  ) {
    if (thumbnailService.isSupported(evaluatedMediaType)) {
      UUID uuid = UUID.randomUUID();

      this.create(Derivative.builder()
        .uuid(UUID.randomUUID())
        .createdBy(ThumbnailService.SYSTEM_GENERATED)
        .dcType(ThumbnailService.THUMBNAIL_DC_TYPE)
        .fileExtension(ThumbnailService.THUMBNAIL_EXTENSION)
        .fileIdentifier(uuid)
        .derivativeType(Derivative.DerivativeType.THUMBNAIL_IMAGE)
        .bucket(bucket)
        .acDerivedFrom(
          this.getReferenceByNaturalId(ObjectStoreMetadata.class, derivedId))
        .build());

      thumbnailService.generateThumbnail(uuid, sourceFilename, evaluatedMediaType, bucket);
    }
  }
}
