package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import ca.gc.aafc.objectstore.api.exceptionmapping.ManagedAttributeChildConflictException;
import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DefaultDinaService;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import lombok.NonNull;

import javax.persistence.criteria.Predicate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ManagedAttributeService extends DefaultDinaService<ManagedAttribute> {

  public ManagedAttributeService(@NonNull BaseDAO baseDAO) {
    super(baseDAO);
  }

  @Override
  protected void preDelete(ManagedAttribute entity) {
    validateChildConflicts(entity);
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
