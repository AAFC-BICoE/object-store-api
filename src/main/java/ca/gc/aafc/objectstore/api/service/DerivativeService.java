package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DefaultDinaService;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import javax.validation.constraints.NotNull;
import java.util.Optional;
import java.util.UUID;

@Service
public class DerivativeService extends DefaultDinaService<Derivative> {
  public DerivativeService(@NonNull BaseDAO baseDAO) {
    super(baseDAO);
  }

  @Override
  protected void preCreate(Derivative entity) {
    entity.setUuid(UUID.randomUUID());
  }

  public Optional<ObjectSubtype> fetchObjectSubtype(@NonNull String acSubtype, @NotNull DcType dcType) {
    return this.findAll(ObjectSubtype.class,
      (criteriaBuilder, objectRoot) -> new Predicate[]{
        criteriaBuilder.equal(objectRoot.get("acSubtype"), acSubtype),
        criteriaBuilder.equal(objectRoot.get("dcType"), dcType),
      }, null, 0, 1)
      .stream()
      .findFirst();
  }

}
