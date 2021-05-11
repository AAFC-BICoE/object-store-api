package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DefaultDinaService;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import ca.gc.aafc.objectstore.api.validation.MetadataManagedAttributeValidator;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class MetaManagedAttributeService extends DefaultDinaService<MetadataManagedAttribute> {

  private final MetadataManagedAttributeValidator metadataManagedAttributeValidator;

  public MetaManagedAttributeService(@NonNull BaseDAO baseDAO, @NonNull MetadataManagedAttributeValidator metadataManagedAttributeValidator) {
    super(baseDAO);
    this.metadataManagedAttributeValidator = metadataManagedAttributeValidator;
  }

  public void validateMetaManagedAttribute(MetadataManagedAttribute entity) {
    validateBusinessRules(entity, metadataManagedAttributeValidator);
  }

  @Override
  protected void preCreate(MetadataManagedAttribute entity) {
    entity.setUuid(UUID.randomUUID());
    updateParentMetadata(entity);
    validateMetaManagedAttribute(entity);
  }

  @Override
  protected void preUpdate(MetadataManagedAttribute entity) {
    updateParentMetadata(entity);
    validateMetaManagedAttribute(entity);
  }

  @Override
  protected void preDelete(MetadataManagedAttribute entity) {
    updateParentMetadata(entity);
  }

  /**
   * MetadataManagedAttribute is considered a child value of ObjectStoreMetadata,
   * so update the parent whenever this is modified.
   * 
   * This helps for auditing.
   */
  private void updateParentMetadata(MetadataManagedAttribute entity) {
    entity.getObjectStoreMetadata().setXmpMetadataDate(OffsetDateTime.now());
  }

}
