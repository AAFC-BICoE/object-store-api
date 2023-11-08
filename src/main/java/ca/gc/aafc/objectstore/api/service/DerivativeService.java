package ca.gc.aafc.objectstore.api.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.validation.SmartValidator;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.messaging.EntityChanged;
import ca.gc.aafc.dina.search.messaging.types.DocumentOperationType;
import ca.gc.aafc.dina.service.MessageProducingService;
import ca.gc.aafc.objectstore.api.dto.DerivativeDto;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.file.FileController;
import ca.gc.aafc.objectstore.api.file.ThumbnailGenerator;
import ca.gc.aafc.objectstore.api.validation.DerivativeValidator;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.ValidationException;
import lombok.NonNull;

@Service
public class DerivativeService extends MessageProducingService<Derivative> {
  private final ThumbnailGenerator thumbnailGenerator;
  private final DerivativeValidator validator;

  public DerivativeService(
    @NonNull BaseDAO baseDAO,
    @NonNull ThumbnailGenerator thumbnailGenerator,
    @NonNull DerivativeValidator validator,
    @NonNull SmartValidator smartValidator,
    ApplicationEventPublisher eventPublisher
  ) {
    super(baseDAO, smartValidator, DerivativeDto.TYPENAME, eventPublisher);
    this.thumbnailGenerator = thumbnailGenerator;
    this.validator = validator;
  }

  @Override
  protected void preCreate(Derivative entity) {
    entity.setUuid(UUID.randomUUID());
    handleFileRelatedData(entity);
    establishBiDirectionalAssociation(entity);
  }

  @Override
  public Derivative create(Derivative entity) {
    Derivative derivative = super.create(entity);
    handleThumbnailGeneration(derivative);
    return derivative;
  }

  @Override
  protected void preUpdate(Derivative entity) {
    handleFileRelatedData(entity);
    establishBiDirectionalAssociation(entity);
  }

  @Override
  public void validateBusinessRules(Derivative entity) {
    applyBusinessRule(entity, validator);
  }

  /**
   * Handle data that is related to file (ObjectUpload).
   * @param derivative
   */
  private void handleFileRelatedData(Derivative derivative) {

    // skip validation for system generated
    if (isSystemGenerated(derivative)) {
      return;
    }

    UUID fileIdentifier = derivative.getFileIdentifier();
    ObjectUpload objectUpload = findOne(
      fileIdentifier,
      ObjectUpload.class);

    // Object Upload must be present, signals a real file has been previously uploaded.
    if (objectUpload == null) {
      throw new ValidationException("Upload with fileIdentifier:" + fileIdentifier + " not found");
    }

    // Object Upload must be a derivative
    if (!objectUpload.getIsDerivative()) {
      throw new ValidationException("Upload with fileIdentifier:" + fileIdentifier + " is not a derivative");
    }

    // Auto populated fields based on object upload for given File Id
    derivative.setFileExtension(objectUpload.getEvaluatedFileExtension());
    derivative.setAcHashValue(objectUpload.getSha1Hex());
    derivative.setAcHashFunction(FileController.DIGEST_ALGORITHM);
    derivative.setBucket(objectUpload.getBucket());
    if (StringUtils.isBlank(derivative.getDcFormat())) { // Auto populate if not submitted
      derivative.setDcFormat(objectUpload.getEvaluatedMediaType());
    }
  }

  @Override
  protected void postPublishEvent(Derivative persisted, DocumentOperationType op) {
    // if we are adding or updating a derivative, we need to notify the metadata since the relationship
    // is hold on the derivative end
    if ((DocumentOperationType.UPDATE == op || DocumentOperationType.ADD == op)
      && persisted.getAcDerivedFrom() != null) {
      EntityChanged event = EntityChanged.builder().op(DocumentOperationType.UPDATE)
        .resourceType(ObjectStoreMetadataDto.TYPENAME)
        .uuid(persisted.getAcDerivedFrom().getUuid()).build();
      publishEvent(event);
    }
  }

  private static void establishBiDirectionalAssociation(Derivative entity) {
    if (entity.getAcDerivedFrom() != null) {
      entity.getAcDerivedFrom().addDerivative(entity);
    }
  }

  public Optional<Derivative> findThumbnailDerivativeForMetadata(ObjectStoreMetadata metadata) {
    return findOneBy((criteriaBuilder, derivativeRoot) -> new Predicate[] {
      criteriaBuilder.equal(derivativeRoot.get("acDerivedFrom"), metadata),
      criteriaBuilder.equal(derivativeRoot.get("derivativeType"),
        Derivative.DerivativeType.THUMBNAIL_IMAGE)
    });
  }

  public Optional<Derivative> findByFileId(UUID fileId) {
    return findOneBy((cb, root) -> new Predicate[] {cb.equal(root.get("fileIdentifier"), fileId)});
  }

  private void handleThumbnailGeneration(@NonNull Derivative resource) {
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

      // do not emit message since the source will already emit one
      super.create(derivative, false);
      thumbnailGenerator.generateThumbnail(
        uuid,
        sourceFilename,
        evaluatedMediaType,
        sourceBucket,
        isSourceDerivative);
    }
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
      // do not emit message since the source will already emit one
      delete(thumbnail.get(), false);
    }
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

  private static boolean isSystemGenerated(Derivative derivative) {
    return
      Derivative.DerivativeType.THUMBNAIL_IMAGE.equals(derivative.getDerivativeType()) &&
        ThumbnailGenerator.SYSTEM_GENERATED.equals(derivative.getCreatedBy());
  }

}
