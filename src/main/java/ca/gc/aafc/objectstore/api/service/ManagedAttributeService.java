package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import ca.gc.aafc.objectstore.api.exceptionmapping.ManagedAttributeChildConflictException;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DefaultDinaService;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.validation.ManagedAttributeValidator;
import lombok.NonNull;

import javax.persistence.criteria.Predicate;
import java.util.List;
import java.util.stream.Collectors;

import java.util.Optional;

@Service
public class ManagedAttributeService extends DefaultDinaService<ManagedAttribute> {

  private final ManagedAttributeValidator managedAttributeValidator;

  public ManagedAttributeService(@NonNull BaseDAO baseDAO, @NonNull ManagedAttributeValidator managedAttributeValidator) {
    super(baseDAO);
    this.managedAttributeValidator = managedAttributeValidator;
  }

  @Override
  protected void preCreate(ManagedAttribute entity) {
    entity.prePersist();
    validateManagedAttribute(entity);
  }

  @Override
  protected void preUpdate(ManagedAttribute entity) {
    entity.preUpdate();
    validateManagedAttribute(entity);
  }

  @Override
  protected void preDelete(ManagedAttribute entity) {
    validateChildConflicts(entity);
  }

  public void validateManagedAttribute(ManagedAttribute entity) {
    Errors errors = new BeanPropertyBindingResult(entity, entity.getUuid().toString());
    managedAttributeValidator.validate(entity, errors);

    if (!errors.hasErrors()) {
      return;
    }

    Optional<String> errorMsg = errors.getAllErrors().stream().map(ObjectError::getDefaultMessage).findAny();
    errorMsg.ifPresent(msg -> {
      throw new IllegalArgumentException(msg);
    });
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
}
