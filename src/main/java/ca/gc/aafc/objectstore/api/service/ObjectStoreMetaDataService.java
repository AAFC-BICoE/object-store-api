package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DefaultDinaService;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import ca.gc.aafc.objectstore.api.resolvers.ObjectStoreMetaDataFieldResolvers;
import lombok.NonNull;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.UUID;

@Service
public class ObjectStoreMetaDataService extends DefaultDinaService<ObjectStoreMetadata> {

  @Inject
  private ObjectStoreMetaDataFieldResolvers metaDataFieldResolver;

  private final ObjectStoreMetadataDefaultValueSetterService defaultValueSetterService;

  private final BaseDAO baseDAO;

  public ObjectStoreMetaDataService(@NonNull BaseDAO baseDAO,
                                    @NonNull ObjectStoreMetadataDefaultValueSetterService defaultValueSetterService) {
    super(baseDAO);
    this.baseDAO = baseDAO;
    this.defaultValueSetterService = defaultValueSetterService;
  }

  @Override
  protected void preCreate(ObjectStoreMetadata entity) {
    entity.setUuid(UUID.randomUUID());

    defaultValueSetterService.assignDefaultValues(entity);

    if (entity.getAcSubType() != null) {
      setAcSubType(entity, entity.getAcSubType());
    }
  }

  @Override
  protected void preUpdate(ObjectStoreMetadata entity) {
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
    ObjectSubtype fetchedType = metaDataFieldResolver.acSubTypeToEntity(
      acSubType.getDcType(),
      acSubType.getAcSubtype());
    metadata.setAcSubType(fetchedType);
  }

}
