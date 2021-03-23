package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DefaultDinaService;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import ca.gc.aafc.objectstore.api.file.ThumbnailService;
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
    return this.findAll(Derivative.class, (criteriaBuilder, root) -> new Predicate[]{
      criteriaBuilder.equal(root.get("fileIdentifier"), fileId)}, null, 0, 1).stream().findFirst();
  }

  public ObjectSubtype getThumbNailSubType() {
    return this.findAll(ObjectSubtype.class,
      (criteriaBuilder, objectRoot) -> new Predicate[]{
        criteriaBuilder.equal(objectRoot.get("acSubtype"), ThumbnailService.THUMBNAIL_AC_SUB_TYPE),
        criteriaBuilder.equal(objectRoot.get("dcType"), ThumbnailService.THUMBNAIL_DC_TYPE),
      }, null, 0, 1)
      .stream()
      .findFirst()
      .orElseThrow(() -> new IllegalArgumentException("A thumbnail subtype is not present"));
  }
}
