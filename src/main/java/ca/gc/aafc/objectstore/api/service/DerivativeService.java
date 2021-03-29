package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DefaultDinaService;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
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

  public Optional<Derivative> findByFileId(UUID fileId) {
    return findAll(Derivative.class,
        (cb, root) -> new Predicate[] { cb.equal(root.get("fileIdentifier"), fileId) },
        null, 0, 1)
        .stream().findFirst();
  }


}
