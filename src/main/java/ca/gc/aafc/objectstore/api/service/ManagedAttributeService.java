package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import ca.gc.aafc.objectstore.api.exceptionmapping.ManagedAttributeChildConflictException;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DefaultDinaService;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.validation.ManagedAttributeValidator;
import lombok.NonNull;

import javax.persistence.criteria.Predicate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ManagedAttributeService extends DefaultDinaService<ManagedAttribute> {

  private final ManagedAttributeValidator managedAttributeValidator;

  public ManagedAttributeService(@NonNull BaseDAO baseDAO, @NonNull ManagedAttributeValidator managedAttributeValidator) {
    super(baseDAO);
    this.managedAttributeValidator = managedAttributeValidator;
  }

  @Override
  protected void preCreate(ManagedAttribute entity) {
    entity.setUuid(UUID.randomUUID());
    cleanDescription(entity);
    validateManagedAttribute(entity);
  }

  @Override
  protected void preUpdate(ManagedAttribute entity) {
    cleanDescription(entity);
    validateManagedAttribute(entity);
  }

  @Override
  protected void preDelete(ManagedAttribute entity) {
    validateChildConflicts(entity);
  }

  public void validateManagedAttribute(ManagedAttribute entity) {
    validateBusinessRules(entity, managedAttributeValidator);
  }

  private void validateChildConflicts(ManagedAttribute entity) {
    List<String> childrenIds = this.findAll(
      MetadataManagedAttribute.class,
      (cb, root) -> new Predicate[]{cb.equal(root.get("managedAttribute"), entity)},
      null, 0, Integer.MAX_VALUE).stream()
      .map(metadataManagedAttribute -> metadataManagedAttribute.getObjectStoreMetadata().getUuid().toString())
      .collect(Collectors.toList());

    if (childrenIds.size() > 0) {
      throw new ManagedAttributeChildConflictException(entity.getUuid().toString(), childrenIds);
    }
  }

  /** Cleans empty strings out of the description. */
  private void cleanDescription(ManagedAttribute entity) {
    if (entity.getDescription() != null) {
      Map<String, String> description = new HashMap<>(entity.getDescription());
      description.entrySet().removeIf(entry -> StringUtils.isBlank(entry.getValue()));
      entity.setDescription(description);
    }
  }
}
