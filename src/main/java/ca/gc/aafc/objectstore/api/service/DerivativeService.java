package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DefaultDinaService;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.file.ThumbnailService;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;

@Service
public class DerivativeService extends DefaultDinaService<Derivative> {
  private final ThumbnailService thumbnailService;

  public DerivativeService(
    @NonNull BaseDAO baseDAO,
    @NonNull ThumbnailService thumbnailService
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
  public Derivative create(Derivative entity) {
    Derivative derivative = super.create(entity);
    handleThumbNailGeneration(derivative);
    return derivative;
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
    return findOneBy((cb, root) -> new Predicate[]{cb.equal(root.get("fileIdentifier"), fileId)});
  }

  private void handleThumbNailGeneration(@NonNull Derivative resource) {
    UUID generatedFromDerivativeUUID = resource.getUuid();
    ObjectStoreMetadata acDerivedFrom = resource.getAcDerivedFrom();
    Derivative.DerivativeType derivativeType = resource.getDerivativeType();

    if (thumbnailShouldBeGenerated(generatedFromDerivativeUUID, acDerivedFrom, derivativeType)) {
      String bucket = resource.getBucket();
      String sourceFilename = resource.getFileIdentifier() + resource.getFileExtension();
      UUID derivedId = acDerivedFrom != null ? acDerivedFrom.getUuid() : null;
      String evaluatedMediaType = this.findOne(resource.getFileIdentifier(), ObjectUpload.class)
        .getEvaluatedMediaType();

      this.generateThumbnail(
        bucket,
        sourceFilename,
        evaluatedMediaType,
        derivedId,
        generatedFromDerivativeUUID);
    }
  }

  public void generateThumbnail(
    @NonNull String bucket,
    @NonNull String sourceFilename,
    String evaluatedMediaType,
    UUID acDerivedFromId,
    UUID generatedFromDerivativeUUID
  ) {
    if (generatedFromDerivativeUUID == null && acDerivedFromId == null) {
      throw new IllegalArgumentException(
        "A thumbnail must be derived from something, expecting at least one of: " +
          "acDerivedFromId, generatedFromDerivativeUUID");
    }

    if (thumbnailService.isSupported(evaluatedMediaType)) {
      UUID uuid = UUID.randomUUID();

      Derivative derivative = generateDerivativeForThumbnail(bucket, uuid);

      if (acDerivedFromId != null) {
        derivative.setAcDerivedFrom(this.getReferenceByNaturalId(ObjectStoreMetadata.class, acDerivedFromId));
      }

      if (generatedFromDerivativeUUID != null) {
        if (!this.exists(Derivative.class, generatedFromDerivativeUUID)) {
          throw new IllegalArgumentException(
            "Derivative with id " + generatedFromDerivativeUUID + " does not exist");
        }
        derivative.setGeneratedFromDerivative(generatedFromDerivativeUUID);
      }

      this.create(derivative);
      thumbnailService.generateThumbnail(uuid, sourceFilename, evaluatedMediaType, bucket);
    }
  }

  private boolean thumbnailShouldBeGenerated(
    UUID generatedFromDerivativeUUID,
    ObjectStoreMetadata acDerivedFrom,
    Derivative.DerivativeType derivativeType
  ) {
    return derivativeType != Derivative.DerivativeType.THUMBNAIL_IMAGE &&
      !(hasThumbnail(generatedFromDerivativeUUID) || hasThumbnail(acDerivedFrom));
  }

  private boolean hasThumbnail(UUID generatedFromDerivativeID) {
    if (generatedFromDerivativeID == null) {
      return false;
    }
    return this.findOneBy((criteriaBuilder, derivativeRoot) -> new Predicate[]{
      criteriaBuilder.equal(derivativeRoot.get("generatedFromDerivative"), generatedFromDerivativeID),
      criteriaBuilder.equal(derivativeRoot.get("derivativeType"), Derivative.DerivativeType.THUMBNAIL_IMAGE)
    }).isPresent();
  }

  private boolean hasThumbnail(ObjectStoreMetadata metadata) {
    if (metadata == null) {
      return false;
    }
    return findOneBy((criteriaBuilder, derivativeRoot) -> new Predicate[]{
      criteriaBuilder.equal(derivativeRoot.get("acDerivedFrom"), metadata),
      criteriaBuilder.equal(derivativeRoot.get("derivativeType"), Derivative.DerivativeType.THUMBNAIL_IMAGE)
    }).isPresent();
  }

  private Optional<Derivative> findOneBy(@NonNull BiFunction<CriteriaBuilder, Root<Derivative>, Predicate[]> crit) {
    return this.findAll(Derivative.class, crit, null, 0, 1).stream().findFirst();
  }

  private static Derivative generateDerivativeForThumbnail(String bucket, UUID uuid) {
    return Derivative.builder()
      .uuid(UUID.randomUUID())
      .createdBy(ThumbnailService.SYSTEM_GENERATED)
      .dcType(ThumbnailService.THUMBNAIL_DC_TYPE)
      .fileExtension(ThumbnailService.THUMBNAIL_EXTENSION)
      .fileIdentifier(uuid)
      .derivativeType(Derivative.DerivativeType.THUMBNAIL_IMAGE)
      .bucket(bucket)
      .build();
  }
}
