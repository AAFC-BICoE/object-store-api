package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DefaultDinaService;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import io.crnk.core.exception.BadRequestException;
import lombok.NonNull;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import javax.persistence.criteria.Predicate;
import java.util.UUID;

@Service
public class ObjectStoreMetaDataService extends DefaultDinaService<ObjectStoreMetadata> {

  private final ObjectStoreMetadataDefaultValueSetterService defaultValueSetterService;

  private final MetaManagedAttributeService metaManagedAttributeService;

  private final BaseDAO baseDAO;

  public static final String THUMBNAIL_EXTENSION = ".jpg";
  public static final String THUMBNAIL_AC_SUB_TYPE = "THUMBNAIL";
  public static final DcType THUMBNAIL_DC_TYPE = DcType.IMAGE;
  public static final String SYSTEM_GENERATED = "System Generated";
  public static final String PDF_FILETYPE = "application/pdf";  


  public ObjectStoreMetaDataService(@NonNull BaseDAO baseDAO,
      @NonNull ObjectStoreMetadataDefaultValueSetterService defaultValueSetterService,
      @NonNull MetaManagedAttributeService metaManagedAttributeService) {
    super(baseDAO);
    this.baseDAO = baseDAO;
    this.defaultValueSetterService = defaultValueSetterService;
    this.metaManagedAttributeService = metaManagedAttributeService;
  }
  
  @Override
  protected void preCreate(ObjectStoreMetadata entity) {
    validateMetaManagedAttribute(entity);  
       
    entity.setUuid(UUID.randomUUID());

    defaultValueSetterService.assignDefaultValues(entity);

    if (entity.getAcSubType() != null) {
      setAcSubType(entity, entity.getAcSubType());
    }

    if (entity.getAcDerivedFrom() != null) {
      entity.getAcDerivedFrom().addDerivative(entity);
    }
    createThrumbnailMeta(entity);   
  }

  private void validateMetaManagedAttribute(ObjectStoreMetadata entity) { 
    if ( entity.getManagedAttribute() != null ) {
      entity.getManagedAttribute().forEach(
          metaManagedAttributeService::validateMetaManagedAttribute
      );
    }
  }

  @Override
  protected void preUpdate(ObjectStoreMetadata entity) {
    validateMetaManagedAttribute(entity);  

    ObjectSubtype temp = entity.getAcSubType();

    if (temp != null) {
      /*
       * Need to flush the entities current state here to allow further JPA
       * transactions
       */
      entity.setAcSubType(null);
      baseDAO.update(entity);

      setAcSubType(entity, temp);
    }
    if (entity.getAcDerivedFrom() != null) {
      entity.getAcDerivedFrom().addDerivative(entity);
    }
  }

  @Override
  protected void preDelete(ObjectStoreMetadata entity) {
    if (CollectionUtils.isNotEmpty(entity.getDerivatives())) {
      entity.getDerivatives().forEach(derived -> derived.setAcDerivedFrom(null));
    }
  }

  /**
   * Set a given ObjectStoreMetadata with database backed acSubType based of the given acSubType.
   *
   * @param metadata  - metadata to set
   * @param acSubType - acSubType to fetch
   */
  private void setAcSubType(
    @NonNull ObjectStoreMetadata metadata,
    @NonNull ObjectSubtype acSubType
  ) {
    if (acSubType.getDcType() == null || StringUtils.isBlank(acSubType.getAcSubtype())) {
      metadata.setAcSubType(null);
    } else {
      ObjectSubtype fetchedType = this.findAll(ObjectSubtype.class,
        (criteriaBuilder, objectRoot) -> new Predicate[]{
          criteriaBuilder.equal(objectRoot.get("acSubtype"), acSubType.getAcSubtype()),
          criteriaBuilder.equal(objectRoot.get("dcType"), acSubType.getDcType()),
        }, null, 0, 1)
        .stream().findFirst().orElseThrow(() -> throwBadRequest(acSubType));
      metadata.setAcSubType(fetchedType);
    }
  }

  private BadRequestException throwBadRequest(ObjectSubtype acSubType) {
    return new BadRequestException(
      acSubType.getAcSubtype() + "/" + acSubType.getDcType() + " is not a valid acSubType/dcType");
  }
/**
   * Returns a {@link ObjectStoreMetadata} for a thumbnail based on the given
   * parent resource and thumbnail identifier. 
   * @param parent    - parent resource metadata of the thumbnail
   * @return {@link ObjectStoreMetadata} for the thumbnail
   */
  private void createThrumbnailMeta(ObjectStoreMetadata parent) {
    ObjectUpload upload = findOne(
      parent.getFileIdentifier(),
      ObjectUpload.class);
    if (upload == null) {
      return;
    }
    UUID thumbUuid = upload.getThumbnailIdentifier();
    if (thumbUuid != null ) {
      ObjectStoreMetadata meta = new ObjectStoreMetadata();
      meta.setFileIdentifier(thumbUuid);
      meta.setAcDerivedFrom(parent);
      meta.setDcType(THUMBNAIL_DC_TYPE);
      meta.setAcSubType(parent.getAcSubType());
      meta.setBucket(parent.getBucket());
      meta.setFileExtension(THUMBNAIL_EXTENSION);
      meta.setOriginalFilename(parent.getOriginalFilename());
      meta.setDcRights(parent.getDcRights());
      meta.setXmpRightsOwner(parent.getXmpRightsOwner());
      meta.setXmpRightsWebStatement(parent.getXmpRightsWebStatement());
      meta.setXmpRightsUsageTerms(parent.getXmpRightsUsageTerms());
      meta.setCreatedBy(SYSTEM_GENERATED);   
      create(meta);
    }
  }  
}
