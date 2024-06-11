package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.PostgresJsonbService;
import ca.gc.aafc.dina.util.UUIDHelper;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreManagedAttribute;
import ca.gc.aafc.objectstore.api.validation.ObjectStoreManagedAttributeValidator;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.SmartValidator;

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
    entity.setUuid(UUIDHelper.generateUUIDv7());
    super.preCreate(entity);
  }

  @Override
  protected void preDelete(ObjectStoreManagedAttribute entity) {
    checkKeysFor(entity.getKey());
  }

  @Override
  public void validateBusinessRules(ObjectStoreManagedAttribute entity) {
    applyBusinessRule(entity, managedAttributeValidator);
  }

  private void checkKeysFor(String key) {
    Integer countFirstLevelKeys = jsonbService.countFirstLevelKeys(
      ObjectStoreManagedAttributeService.METADATA_TABLE_NAME, ObjectStoreManagedAttributeService.MANAGED_ATTRIBUTES_COL_NAME, key);
    if (countFirstLevelKeys > 0) {
      throw new IllegalStateException("Managed attribute key: " + key + ", is currently in use.");
    }
  }

  /**
   * Protection against CT_CONSTRUCTOR_THROW
   */
  @Override
  protected final void finalize(){
    // no-op
  }
}
