package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DefaultDinaService;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import ca.gc.aafc.objectstore.api.resolvers.ObjectStoreMetaDataFieldResolvers;
import ca.gc.aafc.objectstore.api.validation.MetadataManagedAttributeValidator;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import javax.inject.Inject;

import java.util.UUID;

@Service
@SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
public class ObjectStoreMetaDataService extends DefaultDinaService<ObjectStoreMetadata> {

  @Inject
  private ObjectStoreMetaDataFieldResolvers metaDataFieldResolver;

  @Inject
  private MetadataManagedAttributeValidator metadataManagedAttributeValidator;
  private final ObjectStoreMetadataDefaultValueSetterService defaultValueSetterService;

  private final BaseDAO baseDAO;

  public ObjectStoreMetaDataService(@NonNull BaseDAO baseDAO,
      @NonNull ObjectStoreMetadataDefaultValueSetterService defaultValueSetterService) {
    super(baseDAO);
    this.baseDAO = baseDAO;
    this.defaultValueSetterService = defaultValueSetterService;
  }

  private void validateMetaManagedAttribute(ObjectStoreMetadata entity) {
    if (entity.getManagedAttribute() == null) {
      return;
    }

    for (MetadataManagedAttribute mma : entity.getManagedAttribute()) {
      Errors errors = new BeanPropertyBindingResult(mma, mma.getUuid().toString());
      metadataManagedAttributeValidator.validate(mma, errors);
      if (errors != null) {
        String errorMsg = errors.getFieldError() != null ? errors.getFieldError().getDefaultMessage()
            : errors.getAllErrors() != null && errors.getAllErrors().size() > 0
                ? errors.getAllErrors().get(0).getDefaultMessage()
                : null;
        if (!StringUtils.isEmpty(errorMsg)) {
          throw new IllegalArgumentException(errorMsg);
        }
      }
    }
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
   * Set a given ObjectStoreMetadata with database backed acSubType based of the
   * given acSubType.
   *
   * @param metadata  - metadata to set
   * @param acSubType - acSubType to fetch
   */
  private void setAcSubType(@NonNull ObjectStoreMetadata metadata, @NonNull ObjectSubtype acSubType) {
    ObjectSubtype fetchedType = metaDataFieldResolver.acSubTypeToEntity(acSubType.getDcType(),
        acSubType.getAcSubtype());
    metadata.setAcSubType(fetchedType);
  }

}
