package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.validation.ManagedAttributeValidator;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ManagedAttributeService extends ca.gc.aafc.dina.service.ManagedAttributeService<ManagedAttribute> {

  private final ManagedAttributeValidator managedAttributeValidator;

  public ManagedAttributeService(
    @NonNull BaseDAO baseDAO,
    @NonNull ManagedAttributeValidator managedAttributeValidator
  ) {
    super(baseDAO);
    this.managedAttributeValidator = managedAttributeValidator;
  }

  @Override
  protected void preCreate(ManagedAttribute entity) {
    entity.setUuid(UUID.randomUUID());
    cleanDescription(entity);
    validateManagedAttribute(entity);
    super.preCreate(entity);
  }

  @Override
  protected void preUpdate(ManagedAttribute entity) {
    cleanDescription(entity);
    validateManagedAttribute(entity);
  }

  public void validateManagedAttribute(ManagedAttribute entity) {
    validateBusinessRules(entity, managedAttributeValidator);
  }

  /**
   * Cleans empty strings out of the description.
   */
  private void cleanDescription(ManagedAttribute entity) {
    if (entity.getDescription() != null) {
      Map<String, String> description = new HashMap<>(entity.getDescription());
      description.entrySet().removeIf(entry -> StringUtils.isBlank(entry.getValue()));
      entity.setDescription(description);
    }
  }
}
