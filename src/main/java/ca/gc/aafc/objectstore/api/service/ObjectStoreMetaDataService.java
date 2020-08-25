package ca.gc.aafc.objectstore.api.service;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import ca.gc.aafc.objectstore.api.resolvers.ObjectStoreMetaDataFieldResolvers;
import lombok.NonNull;

@Service
public class ObjectStoreMetaDataService extends DinaService<ObjectStoreMetadata> {

  @Inject
  private ObjectStoreMetaDataFieldResolvers metaDataFieldResolver;

  private final BaseDAO baseDAO;

  public ObjectStoreMetaDataService(@NonNull BaseDAO baseDAO) {
    super(baseDAO);
    this.baseDAO = baseDAO;
  }

  @Override
  protected void preCreate(ObjectStoreMetadata entity) {
    if (entity.getAcSubType() != null) {
      ObjectSubtype fetchedType = metaDataFieldResolver.acSubTypeToEntity(
          entity.getAcSubType().getDcType(),
          entity.getAcSubType().getAcSubtype());
      entity.setAcSubType(fetchedType);
    }
  }

  @Override
  protected void preDelete(ObjectStoreMetadata entity) {
    // Do Nothing
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

      ObjectSubtype fetchedType = metaDataFieldResolver.acSubTypeToEntity(
        temp.getDcType(),
        temp.getAcSubtype());
      entity.setAcSubType(fetchedType);
    }
  }

}
