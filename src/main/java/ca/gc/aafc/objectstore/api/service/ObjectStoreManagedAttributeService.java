package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreManagedAttribute;
import ca.gc.aafc.objectstore.api.validation.ObjectStoreManagedAttributeValidator;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ObjectStoreManagedAttributeService extends ca.gc.aafc.dina.service.ManagedAttributeService<ObjectStoreManagedAttribute> {

  private final ObjectStoreManagedAttributeValidator managedAttributeValidator;

  public ObjectStoreManagedAttributeService(
    @NonNull BaseDAO baseDAO,
    @NonNull ObjectStoreManagedAttributeValidator managedAttributeValidator
  ) {
    super(baseDAO, ObjectStoreManagedAttribute.class);
    this.managedAttributeValidator = managedAttributeValidator;
  }

  @Override
  protected void preCreate(ObjectStoreManagedAttribute entity) {
    entity.setUuid(UUID.randomUUID());
    cleanDescription(entity);
    validateManagedAttribute(entity);
    super.preCreate(entity);
  }

  @Override
  protected void preUpdate(ObjectStoreManagedAttribute entity) {
    cleanDescription(entity);
    validateManagedAttribute(entity);
  }

  public void validateManagedAttribute(ObjectStoreManagedAttribute entity) {
    validateBusinessRules(entity, managedAttributeValidator);
  }

  /**
   * Cleans empty strings out of the description.
   */
  private void cleanDescription(ObjectStoreManagedAttribute entity) {
    if (entity.getDescription() != null) {
      Map<String, String> description = new HashMap<>(entity.getDescription());
      description.entrySet().removeIf(entry -> StringUtils.isBlank(entry.getValue()));
      entity.setDescription(description);
    }
  }
}
