package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.PostgresJsonbService;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreManagedAttribute;
import ca.gc.aafc.objectstore.api.validation.ObjectStoreManagedAttributeValidator;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.SmartValidator;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ObjectStoreManagedAttributeService extends ca.gc.aafc.dina.service.ManagedAttributeService<ObjectStoreManagedAttribute> {


  public static final String METADATA_TABLE_NAME = "metadata";
  public static final String MANAGED_ATTRIBUTES_COL_NAME = "managed_attribute_values";

  private final ObjectStoreManagedAttributeValidator managedAttributeValidator;
  private final PostgresJsonbService jsonbService;

  public ObjectStoreManagedAttributeService(
    @NonNull BaseDAO baseDAO,
    @NonNull ObjectStoreManagedAttributeValidator managedAttributeValidator,
    SmartValidator smartValidator,
    @NonNull PostgresJsonbService postgresJsonbService
  ) {
    super(baseDAO, smartValidator, ObjectStoreManagedAttribute.class);
    this.managedAttributeValidator = managedAttributeValidator;
    this.jsonbService = postgresJsonbService;
  }

  @Override
  protected void preCreate(ObjectStoreManagedAttribute entity) {
    entity.setUuid(UUID.randomUUID());
    cleanDescription(entity);
    super.preCreate(entity);
  }

  @Override
  protected void preDelete(ObjectStoreManagedAttribute entity) {
    checkKeysFor(entity.getKey());
  }

  @Override
  protected void preUpdate(ObjectStoreManagedAttribute entity) {
    cleanDescription(entity);
  }

  @Override
  public void validateBusinessRules(ObjectStoreManagedAttribute entity) {
    applyBusinessRule(entity, managedAttributeValidator);
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

  private void checkKeysFor(String key) {
    Integer countFirstLevelKeys = jsonbService.countFirstLevelKeys(
      ObjectStoreManagedAttributeService.METADATA_TABLE_NAME, ObjectStoreManagedAttributeService.MANAGED_ATTRIBUTES_COL_NAME, key);
    if (countFirstLevelKeys > 0) {
      throw new IllegalStateException("Managed attribute key: " + key + ", is currently in use.");
    }
  }
}
