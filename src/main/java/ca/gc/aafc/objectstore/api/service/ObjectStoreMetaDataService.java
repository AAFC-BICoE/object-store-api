package ca.gc.aafc.objectstore.api.service;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.criteria.Predicate;
import javax.validation.ValidationException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.validation.SmartValidator;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.MessageProducingService;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.file.FileController;
import ca.gc.aafc.objectstore.api.validation.ObjectStoreManagedAttributeValueValidator;
import ca.gc.aafc.objectstore.api.validation.ObjectStoreMetadataValidator;

import io.crnk.core.exception.BadRequestException;
import lombok.NonNull;

@Service
public class ObjectStoreMetaDataService extends MessageProducingService<ObjectStoreMetadata> {

  private final BaseDAO baseDAO;
  private final ObjectStoreMetadataDefaultValueSetterService defaultValueSetterService;
  private final DerivativeService derivativeService;
  private final ObjectStoreManagedAttributeValueValidator objectStoreManagedAttributeValueValidator;
  private final ObjectStoreMetadataValidator objectStoreMetadataValidator;

  public ObjectStoreMetaDataService(
    @NonNull BaseDAO baseDAO,
    @NonNull ObjectStoreMetadataDefaultValueSetterService defaultValueSetterService,
    @NonNull DerivativeService derivativeService,
    @NonNull SmartValidator smartValidator,
    @NonNull ObjectStoreManagedAttributeValueValidator objectStoreManagedAttributeValueValidator,
    @NonNull ObjectStoreMetadataValidator objectStoreMetadataValidator,
    ApplicationEventPublisher eventPublisher
  ) {
    super(baseDAO, smartValidator, ObjectStoreMetadataDto.TYPENAME, eventPublisher);
    this.baseDAO = baseDAO;
    this.defaultValueSetterService = defaultValueSetterService;
    this.derivativeService = derivativeService;
    this.objectStoreManagedAttributeValueValidator = objectStoreManagedAttributeValueValidator;
    this.objectStoreMetadataValidator = objectStoreMetadataValidator;
  }

  @Override
  protected void preCreate(ObjectStoreMetadata entity) {
    entity.setUuid(UUID.randomUUID());
    handleFileRelatedData(entity);
    defaultValueSetterService.assignDefaultValues(entity);
    if (entity.getAcSubtype() != null) {
      setAcSubtype(entity, entity.getAcSubtype());
    }
  }

  @Override
  public ObjectStoreMetadata create(ObjectStoreMetadata entity) {
    ObjectStoreMetadata objectStoreMetadata = super.create(entity);
    handleThumbNailGeneration(objectStoreMetadata);
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
  public void validateBusinessRules(ObjectStoreMetadata entity) {
    applyBusinessRule(entity, objectStoreMetadataValidator);
    objectStoreManagedAttributeValueValidator.validate(entity, entity.getManagedAttributeValues());
  }

  /**
   * Set a given ObjectStoreMetadata with database backed acSubtype based of the given acSubtype.
   *
   * @param metadata  - metadata to set
   * @param acSubtype - acSubtype to fetch
   */
  private void setAcSubtype(
    @NonNull ObjectStoreMetadata metadata,
    @NonNull ObjectSubtype acSubtype
  ) {
    if (acSubtype.getDcType() == null || StringUtils.isBlank(acSubtype.getAcSubtype())) {
      metadata.setAcSubtype(null);
      metadata.setAcSubtypeId(null);
    } else {
      ObjectSubtype fetchedType = this.findAll(ObjectSubtype.class,
        (criteriaBuilder, objectRoot) -> new Predicate[]{
          criteriaBuilder.equal(objectRoot.get("acSubtype"), acSubtype.getAcSubtype()),
          criteriaBuilder.equal(objectRoot.get("dcType"), acSubtype.getDcType()),
        }, null, 0, 1)
        .stream().findFirst().orElseThrow(() -> throwBadRequest(acSubtype));
      metadata.setAcSubtype(fetchedType);
      metadata.setAcSubtypeId(fetchedType.getId());
    }
  }

  private BadRequestException throwBadRequest(ObjectSubtype acSubtype) {
    return new BadRequestException(
      acSubtype.getAcSubtype() + "/" + acSubtype.getDcType() + " is not a valid acSubtype/dcType");
  }

  /**
   * Generates a thumbnail for the given resource.
   *
   * @param resource - parent resource metadata of the thumbnail
   */
  private void handleThumbNailGeneration(ObjectStoreMetadata resource) {
    String evaluatedMediaType = resource.getDcFormat();
    String bucket = resource.getBucket();
    UUID derivedId = resource.getUuid();
    String sourceFilename = resource.getFileIdentifier() + resource.getFileExtension();
    derivativeService.generateThumbnail(bucket, sourceFilename, derivedId, evaluatedMediaType, null, false);
  }

  /**
   * Method responsible for dealing with validation and setting of data related to files.
   *
   * @param objectMetadata - The metadata of the data to set.
   * @throws ValidationException If a file identifier was not provided.
   */
  private void handleFileRelatedData(ObjectStoreMetadata objectMetadata)
      throws ValidationException {

    if (!objectMetadata.isExternal()) {

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
        throw new ValidationException("primary object with fileIdentifier not found: " + objectMetadata.getFileIdentifier());
      }
        
      objectMetadata.setFileExtension(objectUpload.getEvaluatedFileExtension());
      objectMetadata.setOriginalFilename(objectUpload.getOriginalFilename());
      objectMetadata.setDcFormat(objectUpload.getEvaluatedMediaType());
      objectMetadata.setAcHashValue(objectUpload.getSha1Hex());
      objectMetadata.setAcHashFunction(FileController.DIGEST_ALGORITHM);
        
    } else {
      objectMetadata.setAcHashValue(objectMetadata.getResourceExternalURI().toString());
    }
  }

  public Optional<ObjectStoreMetadata> loadObjectStoreMetadataByFileId(UUID fileId) {
    return this.findAll(
      ObjectStoreMetadata.class,
      (cb, root) -> new Predicate[]{cb.equal(root.get("fileIdentifier"), fileId)}
      , null, 0, 1)
      .stream().findFirst();
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
