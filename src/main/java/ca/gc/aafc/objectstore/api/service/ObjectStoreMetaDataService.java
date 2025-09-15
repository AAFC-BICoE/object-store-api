package ca.gc.aafc.objectstore.api.service;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.ValidationException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.SmartValidator;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.messaging.DinaEventPublisher;
import ca.gc.aafc.dina.messaging.EntityChanged;
import ca.gc.aafc.dina.service.MessageProducingService;
import ca.gc.aafc.dina.util.UUIDHelper;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.file.FileController;
import ca.gc.aafc.objectstore.api.util.ObjectFilenameUtils;
import ca.gc.aafc.objectstore.api.validation.ObjectStoreManagedAttributeValueValidator;
import ca.gc.aafc.objectstore.api.validation.ObjectStoreMetadataValidator;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

/**
 * Service responsible for handling {@link ObjectStoreMetadata} and its related
 * data.
 * This service will trigger thumbnail creation (if required) and delete the
 * thumbnails on deletion.
 */
@Service
@Log4j2
public class ObjectStoreMetaDataService extends MessageProducingService<ObjectStoreMetadata> {

  private final BaseDAO baseDAO;
  private final ObjectStoreMetadataDefaultValueSetterService defaultValueSetterService;
  private final DerivativeGenerationService derivativeGenerationService;
  private final ObjectStoreManagedAttributeValueValidator objectStoreManagedAttributeValueValidator;
  private final ObjectStoreMetadataValidator objectStoreMetadataValidator;

  public ObjectStoreMetaDataService(
      @NonNull BaseDAO baseDAO,
      @NonNull ObjectStoreMetadataDefaultValueSetterService defaultValueSetterService,
      DerivativeGenerationService derivativeGenerationService,
      @NonNull SmartValidator smartValidator,
      @NonNull ObjectStoreManagedAttributeValueValidator objectStoreManagedAttributeValueValidator,
      @NonNull ObjectStoreMetadataValidator objectStoreMetadataValidator,
      DinaEventPublisher<EntityChanged> eventPublisher) {
    super(baseDAO, smartValidator, ObjectStoreMetadataDto.TYPENAME, eventPublisher);
    this.baseDAO = baseDAO;
    this.defaultValueSetterService = defaultValueSetterService;
    this.derivativeGenerationService = derivativeGenerationService;
    this.objectStoreManagedAttributeValueValidator = objectStoreManagedAttributeValueValidator;
    this.objectStoreMetadataValidator = objectStoreMetadataValidator;
  }

  @Override
  protected void preCreate(ObjectStoreMetadata entity) {
    entity.setUuid(UUIDHelper.generateUUIDv7());
    handleFileRelatedData(entity);
    defaultValueSetterService.assignDefaultValues(entity);
    if (entity.getAcSubtype() != null) {
      setAcSubtype(entity, entity.getAcSubtype());
    }
  }

  @Override
  public ObjectStoreMetadata create(ObjectStoreMetadata entity) {
    ObjectStoreMetadata objectStoreMetadata = super.create(entity);
    derivativeGenerationService.handleThumbnailGeneration(objectStoreMetadata);
    return objectStoreMetadata;
  }

  @Override
  protected void preUpdate(ObjectStoreMetadata entity) {
    ObjectSubtype temp = entity.getAcSubtype();

    if (temp != null) {
      /*
       * Need to flush the entities current state here to allow further JPA
       * transactions
       */
      entity.setAcSubtype(null);
      baseDAO.update(entity);

      setAcSubtype(entity, temp);
    }

    handleFileRelatedData(entity);
  }

  @Override
  protected void preDelete(ObjectStoreMetadata entity) {
    try {
      derivativeGenerationService.deleteGeneratedThumbnail(entity);
    } catch (IOException e) {
      // log the exception but don't block the deletion of metadata
      log.warn(e);
    }
  }

  @Override
  public void validateBusinessRules(ObjectStoreMetadata entity) {
    applyBusinessRule(entity, objectStoreMetadataValidator);
    objectStoreManagedAttributeValueValidator.validate(entity, entity.getManagedAttributes());
  }

  /**
   * Set a given ObjectStoreMetadata with database backed acSubtype based of the
   * given acSubtype.
   *
   * @param metadata  - metadata to set
   * @param acSubtype - acSubtype to fetch
   */
  private void setAcSubtype(
      @NonNull ObjectStoreMetadata metadata,
      @NonNull ObjectSubtype acSubtype) {
    if (acSubtype.getDcType() == null || StringUtils.isBlank(acSubtype.getAcSubtype())) {
      metadata.setAcSubtype(null);
      metadata.setAcSubtypeId(null);
    } else {
      ObjectSubtype fetchedType = this.findAll(ObjectSubtype.class,
          (criteriaBuilder, objectRoot) -> new Predicate[] {
              criteriaBuilder.equal(objectRoot.get("acSubtype"), acSubtype.getAcSubtype()),
              criteriaBuilder.equal(objectRoot.get("dcType"), acSubtype.getDcType()),
          }, null, 0, 1)
          .stream().findFirst().orElseThrow(() -> throwBadRequest(acSubtype));
      metadata.setAcSubtype(fetchedType);
      metadata.setAcSubtypeId(fetchedType.getId());
    }
  }

  private IllegalArgumentException throwBadRequest(ObjectSubtype acSubtype) {
    return new IllegalArgumentException(
      acSubtype.getAcSubtype() + "/" + acSubtype.getDcType() + " is not a valid acSubtype/dcType");
  }

  /**
   * Method responsible for dealing with validation and setting of data related to
   * files.
   *
   * @param objectMetadata - The metadata of the data to set.
   * @throws ValidationException If a file identifier was not provided.
   */
  private void handleFileRelatedData(ObjectStoreMetadata objectMetadata)
      throws ValidationException {

    // not required for externally hosted resource
    if (objectMetadata.isExternal()) {
      return;
    }
    // we need to validate at least that bucket name and fileIdentifier are there
    if (StringUtils.isBlank(objectMetadata.getBucket())
        || StringUtils.isBlank(Objects.toString(objectMetadata.getFileIdentifier(), ""))) {
      throw new ValidationException("fileIdentifier and bucket should be provided");
    }

    ObjectUpload objectUpload = this.findOne(
        objectMetadata.getFileIdentifier(),
        ObjectUpload.class);

    // make sure that there is an ObjectUpload that is not a derivative
    if (objectUpload == null || objectUpload.getIsDerivative()) {
      throw new ValidationException(
          "object-upload with fileIdentifier not found: " + objectMetadata.getFileIdentifier());
    }

    // the following data are considered immutable and are taken directly from the
    // object-upload
    objectMetadata.setFileExtension(objectUpload.getEvaluatedFileExtension());
    objectMetadata.setOriginalFilename(objectUpload.getOriginalFilename());
    objectMetadata.setDcFormat(objectUpload.getEvaluatedMediaType());

    // fill filename but only if it has no value
    if (StringUtils.isBlank(objectMetadata.getFilename())) {
      objectMetadata.setFilename(ObjectFilenameUtils.standardizeFilename(objectUpload.getOriginalFilename()));
    } else {
      objectMetadata.setFilename(ObjectFilenameUtils.standardizeFilename(objectMetadata.getFilename()));
    }

    // if the sha1hex is unspecified, do not alter the value on the metadata
    // see https://github.com/AAFC-BICoE/object-store-api/releases/tag/v1.4
    if (StringUtils.isNotBlank(objectUpload.getSha1Hex()) && !"0".equals(objectUpload.getSha1Hex())) {
      objectMetadata.setAcHashValue(objectUpload.getSha1Hex());
      objectMetadata.setAcHashFunction(FileController.DIGEST_ALGORITHM);
    }
  }

  /**
   * Returns an Optional ObjectStoreMetadata for a given criteria.
   *
   * @param crit criteria to find the derivative
   * @return an Optional Derivative for a given criteria.
   */
  private Optional<ObjectStoreMetadata> findOneBy(
    @NonNull BiFunction<CriteriaBuilder, Root<ObjectStoreMetadata>, Predicate[]> crit) {
    return this.findAll(ObjectStoreMetadata.class, crit, null, 0, 1).stream().findFirst();
  }

  public Optional<ObjectStoreMetadata> findByFileId(UUID fileId) {
    return findOneBy((cb, root) -> new Predicate[] {cb.equal(root.get("fileIdentifier"), fileId)});
  }

  /**
   * findOne implementation specific to ObjectStoreMetadata
   *
   * @param uuid
   * @return
   */
  public ObjectStoreMetadata findOne(UUID uuid) {
    return findOne(uuid, ObjectStoreMetadata.class);
  }

}
