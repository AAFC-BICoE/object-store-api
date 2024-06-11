package ca.gc.aafc.objectstore.api.service;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import org.springframework.stereotype.Service;
import org.springframework.validation.SmartValidator;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DefaultDinaService;
import ca.gc.aafc.dina.util.UUIDHelper;
import ca.gc.aafc.objectstore.api.entities.AbstractObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.file.ThumbnailGenerator;
import ca.gc.aafc.objectstore.api.storage.FileStorage;

/**
 * Service responsible for automatic derivative generation (only thumbnail for now)
 */
@Log4j2
@Service
public class DerivativeGenerationService extends DefaultDinaService<Derivative> {

  private final FileStorage fileStorage;
  private final ThumbnailGenerator thumbnailGenerator;

  public DerivativeGenerationService(@NonNull BaseDAO baseDAO,
                                     @NonNull SmartValidator validator,
                                     FileStorage fileStorage,
                                     @NonNull ThumbnailGenerator thumbnailGenerator) {
    super(baseDAO, validator);
    this.fileStorage = fileStorage;
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

  /**
   * Generates a thumbnail for the given Derivative if required/possible.
   *
   * @param resource - parent resource metadata of the thumbnail
   */
  public void handleThumbnailGeneration(@NonNull Derivative resource) {
    ObjectStoreMetadata acDerivedFrom = resource.getAcDerivedFrom();
    Derivative.DerivativeType derivativeType = resource.getDerivativeType();

    if (thumbnailShouldBeGenerated(acDerivedFrom, derivativeType)) {
      this.generateThumbnail(
        resource.getBucket(),
        resource.getFilename(),
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
      UUID uuid = UUIDHelper.generateUUIDv7();
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

      // notify the other end of the relationship
      refresh(derivative.getAcDerivedFrom());

      thumbnailGenerator.generateThumbnail(
        uuid,
        sourceFilename,
        evaluatedMediaType,
        sourceBucket,
        isSourceDerivative);
    }
  }

  /**
   * An incomplete derivative is when the record exists in the database but the file is missing
   * in the file storage.
   * It can happen if the api is restarted and the queue for derivatives is not empty.
   *
   * If the file already exist this method will simply return.
   *
   * @param thumbnailDerivative a derivative entity from the database that represents the thumbnail
   */
  public void fixIncompleteThumbnail(Derivative thumbnailDerivative) {

    if(thumbnailDerivative.getDerivativeType() != Derivative.DerivativeType.THUMBNAIL_IMAGE) {
      throw new IllegalStateException("DerivativeType needs to be THUMBNAIL_IMAGE");
    }

    try {
      Optional<?> file =
        fileStorage.getFileInfo(thumbnailDerivative.getBucket(), thumbnailDerivative.getFilename(), true);
      if(file.isPresent()) {
        log.info("Thumbnail file already present, skipping");
        return;
      }
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }

    // Get source
    AbstractObjectStoreMetadata source = thumbnailDerivative.getGeneratedFromDerivative();
    if (source == null) {
      source = thumbnailDerivative.getAcDerivedFrom();
    }

    thumbnailGenerator.generateThumbnail(
      thumbnailDerivative.getFileIdentifier(),
      source.getFilename(),
      source.getDcFormat(),
      source.getBucket(),
      thumbnailDerivative.getGeneratedFromDerivative() != null);
  }

  public Optional<Derivative> findThumbnailDerivativeForMetadata(ObjectStoreMetadata metadata) {
    return findOneBy((criteriaBuilder, derivativeRoot) -> new Predicate[] {
      criteriaBuilder.equal(derivativeRoot.get(Derivative.AC_DERIVED_FROM_PROP), metadata),
      criteriaBuilder.equal(derivativeRoot.get(Derivative.DERIVATIVE_TYPE_PROP),
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
      .uuid(UUIDHelper.generateUUIDv7())
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

  /**
   * Protection against CT_CONSTRUCTOR_THROW
   */
  @Override
  protected final void finalize(){
    // no-op
  }
}
