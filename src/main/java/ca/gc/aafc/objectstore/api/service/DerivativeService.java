package ca.gc.aafc.objectstore.api.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.SmartValidator;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.messaging.DinaEventPublisher;
import ca.gc.aafc.dina.messaging.EntityChanged;
import ca.gc.aafc.dina.messaging.message.DocumentOperationType;
import ca.gc.aafc.dina.service.MessageProducingService;
import ca.gc.aafc.dina.util.UUIDHelper;
import ca.gc.aafc.objectstore.api.dto.DerivativeDto;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.file.FileController;
import ca.gc.aafc.objectstore.api.validation.DerivativeValidator;

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

  private final DerivativeGenerationService derivativeGenerationService;

  private final DerivativeValidator validator;

  public DerivativeService(
    @NonNull BaseDAO baseDAO,
    DerivativeGenerationService derivativeGenerationService,
    @NonNull DerivativeValidator validator,
    @NonNull SmartValidator smartValidator,
    DinaEventPublisher<EntityChanged> eventPublisher
  ) {
    super(baseDAO, smartValidator, DerivativeDto.TYPENAME, eventPublisher);
    this.derivativeGenerationService = derivativeGenerationService;
    this.validator = validator;
  }

  @Override
  protected void preCreate(Derivative entity) {
    entity.setUuid(UUIDHelper.generateUUIDv7());
    handleFileRelatedData(entity);
    establishBiDirectionalAssociation(entity);
  }

  @Override
  public Derivative create(Derivative entity) {
    Derivative derivative = super.create(entity);
    derivativeGenerationService.handleThumbnailGeneration(derivative);
    return derivative;
  }

  @Override
  protected void preUpdate(Derivative entity) {
    handleFileRelatedData(entity);
    establishBiDirectionalAssociation(entity);
  }

  @Override
  protected void preDelete(Derivative entity) {
    removeBiDirectionalAssociation(entity);
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
    if (DerivativeGenerationService.isSystemGenerated(derivative)) {
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

  private static void removeBiDirectionalAssociation(Derivative entity) {
    if (entity.getAcDerivedFrom() != null) {
      entity.getAcDerivedFrom().removeDerivative(entity);
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
}
