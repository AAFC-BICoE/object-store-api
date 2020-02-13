package ca.gc.aafc.objectstore.api.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.gc.aafc.objecstore.api.repository.BaseRepositoryTest;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.interfaces.SoftDeletable;
import ca.gc.aafc.objectstore.api.respository.ObjectStoreResourceRepository;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;

public class RsqlFilterHandlerIT extends BaseRepositoryTest {
  
  @Inject
  private EntityManager entityManager;

  @Inject
  private ObjectStoreResourceRepository objectStoreResourceRepository;

  private ObjectStoreMetadata testObjectStoreMetadata1;
  private ObjectStoreMetadata testObjectStoreMetadata2;

  @BeforeEach
  public void setup() { 
    testObjectStoreMetadata1 = ObjectStoreMetadataFactory.newObjectStoreMetadata()
      .bucket("bucket1").build();
    persist(testObjectStoreMetadata1);

    testObjectStoreMetadata2 = ObjectStoreMetadataFactory.newObjectStoreMetadata()
      .bucket("bucket2").build();
    persist(testObjectStoreMetadata2);
  }

  @Test
  public void findAllMetadatas_whenRsqlFilterIsApplied_resultListIsFiltered() {
    QuerySpec querySpec = new QuerySpec(ObjectStoreMetadataDto.class);
    querySpec.addFilter(
      new FilterSpec(
        Collections.singletonList("rsql"),
        FilterOperator.EQ,
        "bucket==bucket2"
      )
    );

    // The results should be filtered where the bucket is "bucket2":
    ResourceList<ObjectStoreMetadataDto> results = objectStoreResourceRepository.findAll(querySpec);
    assertEquals(1, results.size());
    assertEquals("bucket2", results.get(0).getBucket());
  }

  @Test
  public void findAllMetadatas_whenRsqlFilterIsBlank_resultListIsNotFiltered() {
    QuerySpec querySpec = new QuerySpec(ObjectStoreMetadataDto.class);
    querySpec.setLimit(1000L);
    querySpec.addFilter(
      new FilterSpec(
        Collections.singletonList("rsql"),
        FilterOperator.EQ,
        ""
      )
    );

    // Get the total count of metadatas in the database where deletedDate is null:
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<ObjectStoreMetadata> criteriaQuery = cb.createQuery(ObjectStoreMetadata.class);
    Root<ObjectStoreMetadata> metadata = criteriaQuery.from(ObjectStoreMetadata.class);
    criteriaQuery.select(metadata).where(metadata.get(SoftDeletable.DELETED_DATE_FIELD_NAME).isNull());
    TypedQuery<ObjectStoreMetadata> typedQuery = entityManager.createQuery(criteriaQuery);
    List<ObjectStoreMetadata> resultList = typedQuery.getResultList();

    // The results should not have been filtered:
    ResourceList<ObjectStoreMetadataDto> results = objectStoreResourceRepository.findAll(querySpec);

    assertEquals(resultList.size(), results.size());
  }
}
