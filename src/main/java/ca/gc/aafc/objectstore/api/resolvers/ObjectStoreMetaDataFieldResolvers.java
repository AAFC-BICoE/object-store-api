package ca.gc.aafc.objectstore.api.resolvers;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;

@Component
public class ObjectStoreMetaDataFieldResolvers {

  private final EntityManager entityManager;

  @Inject
  public ObjectStoreMetaDataFieldResolvers(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  public static String acSubTypeToDTO(ObjectSubtype aSubtype) {
    return aSubtype == null ? null : aSubtype.getAcSubtype();
  }

  public ObjectSubtype acSubTypeToEntity(DcType dcType, String acSubType) {
    if (dcType == null || StringUtils.isBlank(acSubType)) {
      return null;
    }

    return getObjectSubType(dcType, acSubType);
  }

  private ObjectSubtype getObjectSubType(DcType dcType, String acSubType) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<ObjectSubtype> query = criteriaBuilder.createQuery(ObjectSubtype.class);
    Root<ObjectSubtype> root = query.from(ObjectSubtype.class);

    Predicate[] predicates = new Predicate[2];
    predicates[0] = criteriaBuilder.equal(root.get("dcType"), dcType);
    predicates[1] = criteriaBuilder.equal(root.get("acSubtype"), acSubType);

    query.select(root).where(predicates);
    TypedQuery<ObjectSubtype> results = entityManager.createQuery(query);

    return results.getSingleResult();
  }

}
