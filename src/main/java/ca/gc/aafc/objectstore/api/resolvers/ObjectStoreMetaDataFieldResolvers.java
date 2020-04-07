package ca.gc.aafc.objectstore.api.resolvers;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import ca.gc.aafc.dina.mapper.JpaDtoMapper.CustomFieldResolverSpec;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import io.crnk.core.exception.BadRequestException;

/**
 * Field resolvers for the
 * {@link ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata} Class
 */
@Component
public class ObjectStoreMetaDataFieldResolvers {

  private final EntityManager entityManager;

  @Inject
  public ObjectStoreMetaDataFieldResolvers(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  /**
   * Returns the {@link CustomFieldResolverSpec} needed to map
   * {@link ObjectStoreMetadataDto} fields from the {@link ObjectStoreMetadata}
   * class.
   * 
   * @return List of {@link CustomFieldResolverSpec}
   */
  public List<CustomFieldResolverSpec<?>> getDtoResolvers() {
    return Arrays.asList(
      CustomFieldResolverSpec.<ObjectStoreMetadata>builder()
        .field("acSubType")
        .resolver(metadata -> acSubTypeToDTO(metadata.getAcSubType()))
        .build()
    );
  }

  /**
   * Returns the {@link CustomFieldResolverSpec} needed to map
   * {@link ObjectStoreMetadata} fields from the {@link ObjectStoreMetadataDto}
   * class.
   * 
   * @return List of {@link CustomFieldResolverSpec}
   */
  public List<CustomFieldResolverSpec<?>> getEntityResolvers() {
    return Arrays.asList(
      CustomFieldResolverSpec.<ObjectStoreMetadataDto>builder()
        .field("acSubType")
        .resolver(metadataDTO -> acSubTypeToEntity(metadataDTO.getDcType(), metadataDTO.getAcSubType()))
        .build()
    );
  }

  /**
   * Returns the AcSubType of the given {@link ObjectSubtype}. Null is returned if
   * the given {@link ObjectSubtype} is null.
   * 
   * @param aSubtype - {@link ObjectSubtype} to map.
   * @return AcSubType of the given {@link ObjectSubtype}
   */
  private static String acSubTypeToDTO(ObjectSubtype aSubtype) {
    return aSubtype == null ? null : aSubtype.getAcSubtype();
  }

  /**
   * Returns an {@link ObjectSubtype} from the database with a given dcType and
   * acSubType. Null is returned if the dcType or acSubType is blank. Throws
   * {@link BadRequestException} If a match is not found.
   * 
   * @param dcType    - dcType to match
   * @param acSubType - acSubType to match
   * @throws BadRequestException If a match is not found.
   * @return {@link ObjectSubtype} from the database
   */
  private ObjectSubtype acSubTypeToEntity(DcType dcType, String acSubType) {
    if (dcType == null || StringUtils.isBlank(acSubType)) {
      return null;
    }

    return getObjectSubType(dcType, acSubType);
  }

  /**
   * Returns an {@link ObjectSubtype} from the database with a given dcType and
   * acSubType. Throws {@link BadRequestException} If a match is not found.
   * 
   * @param dcType    - dcType to match
   * @param acSubType - acSubType to match
   * @throws BadRequestException If a match is not found.
   * @return {@link ObjectSubtype} from the database
   */
  private ObjectSubtype getObjectSubType(DcType dcType, String acSubType) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<ObjectSubtype> query = criteriaBuilder.createQuery(ObjectSubtype.class);
    Root<ObjectSubtype> root = query.from(ObjectSubtype.class);

    Predicate[] predicates = new Predicate[2];
    predicates[0] = criteriaBuilder.equal(root.get("dcType"), dcType);
    predicates[1] = criteriaBuilder.equal(root.get("acSubtype"), acSubType);

    query.select(root).where(predicates);
    TypedQuery<ObjectSubtype> results = entityManager.createQuery(query);

    return results.getResultList()
      .stream()
      .findFirst()
      .orElseThrow(() -> 
        new BadRequestException(acSubType + "/" + dcType + " is not a valid acSubType/dcType"));
  }

}
