package ca.gc.aafc.objectstore.api.service;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.NonNull;

import org.springframework.stereotype.Service;
import org.springframework.validation.SmartValidator;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DefaultDinaService;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.file.ThumbnailGenerator;

/**
 * Service responsible for automatic derivative generation (only thumbnail for now)
 */
@Service
public class DerivativeGenerationService extends DefaultDinaService<Derivative> {

  private final ThumbnailGenerator thumbnailGenerator;

  public DerivativeGenerationService(@NonNull BaseDAO baseDAO,
                                     @NonNull SmartValidator validator,
                                     @NonNull ThumbnailGenerator thumbnailGenerator) {
    super(baseDAO, validator);
    this.thumbnailGenerator = thumbnailGenerator;
  }

  /**
   * Generates a thumbnail for the given resource if required/possible.
   *
   * @param resource - parent resource metadata of the thumbnail
   */
  public void handleThumbnailGeneration(ObjectStoreMetadata resource) {

    // we don't try to generate a thumbnail for external resources
    if (resource.isExternal()) {
      return;
    }

    String evaluatedMediaType = resource.getDcFormat();
    String bucket = resource.getBucket();
    UUID derivedId = resource.getUuid();
    String sourceFilename = resource.getFileIdentifier() + resource.getFileExtension();
    Boolean publiclyReleasable = resource.getPubliclyReleasable();
    generateThumbnail(bucket, sourceFilename, derivedId, evaluatedMediaType, null, false,
      publiclyReleasable);
  }

  public void handleThumbnailGeneration(@NonNull Derivative resource) {
    ObjectStoreMetadata acDerivedFrom = resource.getAcDerivedFrom();
    Derivative.DerivativeType derivativeType = resource.getDerivativeType();

    if (thumbnailShouldBeGenerated(acDerivedFrom, derivativeType)) {
      this.generateThumbnail(
        resource.getBucket(),
        resource.getFileIdentifier() + resource.getFileExtension(),
        acDerivedFrom.getUuid(),
        resource.getDcFormat(),
        resource.getUuid(),
        true,
        acDerivedFrom.getPubliclyReleasable());
    }
  }

  /**
   * Generates a thumbnail for a resource with the given parameters if possible based on the evaluatedMediaType.
   *
   * No messages will be emitted if a derivative is created for the thumbnail.
   *
   * @param sourceBucket                bucket of the resource
   * @param sourceFilename              file name of the resource
   * @param acDerivedFromId             metadata id of the original resource
   * @param evaluatedMediaType          evaluated media type of the resource, can be null
   * @param generatedFromDerivativeUUID id of the derivative this resource derives from, can be null
   * @param isSourceDerivative          true if the source of the thumbnail is a derivative
   * @param publiclyReleasable          Is the entity considered publicly releasable?
   */
  public void generateThumbnail(
    @NonNull String sourceBucket,
    @NonNull String sourceFilename,
    @NonNull UUID acDerivedFromId,
    String evaluatedMediaType,
    UUID generatedFromDerivativeUUID,
    boolean isSourceDerivative,
    Boolean publiclyReleasable
  ) {
    if (ThumbnailGenerator.isSupported(evaluatedMediaType)) {
      UUID uuid = UUID.randomUUID();
      Derivative derivative = generateDerivativeForThumbnail(sourceBucket, uuid, publiclyReleasable);

      if (!this.exists(ObjectStoreMetadata.class, acDerivedFromId)) {
        throw new IllegalArgumentException(
          "ObjectStoreMetadata with id " + acDerivedFromId + " does not exist");
      }
      derivative.setAcDerivedFrom(
        this.getReferenceByNaturalId(ObjectStoreMetadata.class, acDerivedFromId));

      if (generatedFromDerivativeUUID != null) {
        if (!this.exists(Derivative.class, generatedFromDerivativeUUID)) {
          throw new IllegalArgumentException(
            "Derivative with id " + generatedFromDerivativeUUID + " does not exist");
        }
        derivative.setGeneratedFromDerivative(
          this.getReferenceByNaturalId(Derivative.class, generatedFromDerivativeUUID));
      }

      super.create(derivative);
      thumbnailGenerator.generateThumbnail(
        uuid,
        sourceFilename,
        evaluatedMediaType,
        sourceBucket,
        isSourceDerivative);
    }
  }


  public Optional<Derivative> findThumbnailDerivativeForMetadata(ObjectStoreMetadata metadata) {
    return findOneBy((criteriaBuilder, derivativeRoot) -> new Predicate[] {
      criteriaBuilder.equal(derivativeRoot.get("acDerivedFrom"), metadata),
      criteriaBuilder.equal(derivativeRoot.get("derivativeType"),
        Derivative.DerivativeType.THUMBNAIL_IMAGE)
    });
  }

  /**
   * Returns an Optional Derivative for a given criteria.
   *
   * @param crit criteria to find the derivative
   * @return an Optional Derivative for a given criteria.
   */
  private Optional<Derivative> findOneBy(
    @NonNull BiFunction<CriteriaBuilder, Root<Derivative>, Predicate[]> crit) {
    return this.findAll(Derivative.class, crit, null, 0, 1).stream().findFirst();
  }

  /**
   * Returns true if a thumbnail should be generated. A thumbnail should be generated if a given Derivative
   * type is not a thumbnail , has an ac derived from, and does not already have a thumbnail for the ac
   * derived from.
   *
   * @param acDerivedFrom  metadata to check.
   * @param derivativeType type of derivative
   * @return Returns true if a thumbnail should be generated.
   */
  private boolean thumbnailShouldBeGenerated(
    ObjectStoreMetadata acDerivedFrom,
    Derivative.DerivativeType derivativeType
  ) {
    return derivativeType != Derivative.DerivativeType.THUMBNAIL_IMAGE &&
      acDerivedFrom != null &&
      findThumbnailDerivativeForMetadata(acDerivedFrom).isEmpty();
  }

  /**
   * Generates a Template for a derivative to be used with thumbnails.
   *
   * @param bucket         bucket of the derivative
   * @param fileIdentifier file identifier for the thumbnail
   * @param publiclyReleasable          Is the entity considered publicly releasable?
   * @return a Template for a derivative to be used with thumbnails
   */
  private static Derivative generateDerivativeForThumbnail(String bucket, UUID fileIdentifier, Boolean publiclyReleasable) {
    return Derivative.builder()
      .uuid(UUID.randomUUID())
      .createdBy(ThumbnailGenerator.SYSTEM_GENERATED)
      .dcType(ThumbnailGenerator.THUMBNAIL_DC_TYPE)
      .fileExtension(ThumbnailGenerator.THUMBNAIL_EXTENSION)
      .fileIdentifier(fileIdentifier)
      .dcFormat(ThumbnailGenerator.THUMB_DC_FORMAT)
      .derivativeType(Derivative.DerivativeType.THUMBNAIL_IMAGE)
      .bucket(bucket)
      .publiclyReleasable(publiclyReleasable)
      .build();
  }

  /**
   * If found, delete the system generated thumbnail attached to the provided metadata.
   * This method will delete the file in MinIO and the derivative record.
   *
   * No messages will be emitted for the deleted derivative.
   *
   * @param metadata
   */
  public void deleteGeneratedThumbnail(ObjectStoreMetadata metadata) throws IOException {
    Optional<Derivative> thumbnail = findThumbnailDerivativeForMetadata(metadata);
    if (thumbnail.isPresent() &&
      ThumbnailGenerator.SYSTEM_GENERATED.equals(thumbnail.get().getCreatedBy())) {
      thumbnailGenerator.deleteThumbnail(thumbnail.get().getFileIdentifier(),
        thumbnail.get().getBucket());
      delete(thumbnail.get());
    }
  }

  /**
   * Consistent with generateDerivativeForThumbnail but should be based on a boolean fields instead
   * createdBy
   * @param derivative
   * @return
   */
  static boolean isSystemGenerated(Derivative derivative) {
    return
      Derivative.DerivativeType.THUMBNAIL_IMAGE.equals(derivative.getDerivativeType()) &&
        ThumbnailGenerator.SYSTEM_GENERATED.equals(derivative.getCreatedBy());
  }
}
