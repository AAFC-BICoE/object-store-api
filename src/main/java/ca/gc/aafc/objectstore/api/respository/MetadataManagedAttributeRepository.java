package ca.gc.aafc.objectstore.api.respository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;

import ca.gc.aafc.objectstore.api.dao.BaseDAO;
import ca.gc.aafc.objectstore.api.dto.MetadataManagedAttributeDto;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.filter.RsqlFilterHandler;
import ca.gc.aafc.objectstore.api.interfaces.SoftDeletable;
import ca.gc.aafc.objectstore.api.mapper.CycleAvoidingMappingContext;
import ca.gc.aafc.objectstore.api.mapper.MetadataManagedAttributeMapper;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.data.jpa.query.JpaQueryExecutor;
import io.crnk.data.jpa.query.criteria.JpaCriteriaQuery;
import io.crnk.data.jpa.query.criteria.JpaCriteriaQueryFactory;

@Repository
@Transactional
public class MetadataManagedAttributeRepository extends ResourceRepositoryBase<MetadataManagedAttributeDto, UUID> {

  private final BaseDAO dao;
  private final MetadataManagedAttributeMapper mapper;
  private final RsqlFilterHandler rsqlFilterHandler;
  
  private JpaCriteriaQueryFactory queryFactory;

  private static PathSpec DELETED_PATH_SPEC = PathSpec.of(SoftDeletable.DELETED_DATE_FIELD_NAME);
  private static FilterSpec DELETED_DATE_IS_NULL = new FilterSpec(DELETED_PATH_SPEC, FilterOperator.EQ, null);

  @Inject
  public MetadataManagedAttributeRepository(
    BaseDAO dao,
    MetadataManagedAttributeMapper mapper,
    RsqlFilterHandler rsqlFilterHandler) {
    super(MetadataManagedAttributeDto.class);
    this.dao = dao;
    this.mapper = mapper;
    this.rsqlFilterHandler = rsqlFilterHandler;
  }
  
  @PostConstruct
  void setup() {
    queryFactory = dao.createWithEntityManager(JpaCriteriaQueryFactory::newInstance);
  }

  /**
   * @param resource
   *          to save
   * @return saved resource
   */
  @Override
  public <S extends MetadataManagedAttributeDto> S save(S resource) {
    MetadataManagedAttributeDto dto =  (MetadataManagedAttributeDto) resource ;
    MetadataManagedAttribute metadataManagedAttribute = dao.findOneByNaturalId(dto.getUuid(), MetadataManagedAttribute.class);
    mapper.updateMetadataManagedAttributeFromDto(dto, metadataManagedAttribute);
    
    System.out.println("Saving current objectStoreMetadata:" + metadataManagedAttribute.getObjectStoreMetadata());
    dao.save(metadataManagedAttribute);
    return resource;
  }

  @Override
  public MetadataManagedAttributeDto findOne(UUID uuid, QuerySpec querySpec) {
    MetadataManagedAttribute metadataManagedAttribute = dao.findOneByNaturalId(uuid, MetadataManagedAttribute.class);
    if(metadataManagedAttribute ==null){
    // Throw the 404 exception if the resource is not found.
      throw new ResourceNotFoundException(
          this.getClass().getSimpleName() + " with ID " + uuid + " Not Found."
      );
    }

    if (metadataManagedAttribute.getDeletedDate() != null
        && !querySpec.findFilter(DELETED_PATH_SPEC).isPresent()) {
      // Throw the 410 exception if the resource is not found.
      throw new GoneException("ID " + uuid + " deleted");
    }

    return mapper.toDto(metadataManagedAttribute, new CycleAvoidingMappingContext());
  }

  @Override
  public ResourceList<MetadataManagedAttributeDto> findAll(QuerySpec querySpec) {
    JpaCriteriaQuery<MetadataManagedAttribute> jq = queryFactory.query(MetadataManagedAttribute.class);

    if (!querySpec.findFilter(DELETED_PATH_SPEC).isPresent()) {
      querySpec.addFilter(DELETED_DATE_IS_NULL);
    }

    Consumer<JpaQueryExecutor<?>> rsqlApplier = rsqlFilterHandler.getRestrictionApplier(querySpec);
    JpaQueryExecutor<MetadataManagedAttribute> executor = jq.buildExecutor(querySpec);
    rsqlApplier.accept(executor);

    List<MetadataManagedAttributeDto> l = executor.getResultList().stream()
    .map( e -> mapper.toDto(e, new CycleAvoidingMappingContext()))
    .collect(Collectors.toList());
    
    return new DefaultResourceList<MetadataManagedAttributeDto>(l, null, null);
  }

  @Override
  public <S extends MetadataManagedAttributeDto> S create(S resource) {
    MetadataManagedAttributeDto dto =  (MetadataManagedAttributeDto) resource ;
    if(dto.getUuid()==null) {
      dto.setUuid(UUID.randomUUID());
    }
    
    MetadataManagedAttribute metadataManagedAttribute = mapper
        .toEntity((MetadataManagedAttributeDto) resource);
    
    // relationships
    if (dto.getManagedAttribute() != null) {
      metadataManagedAttribute.setManagedAttribute(dao.getReferenceByNaturalId(ManagedAttribute.class, dto.getManagedAttribute().getUuid()));
    }
    
    if(dto.getObjectStoreMetadata() != null) {
      metadataManagedAttribute.setObjectStoreMetadata(dao.getReferenceByNaturalId(ObjectStoreMetadata.class, dto.getObjectStoreMetadata().getUuid()));
    }

    dao.save(metadataManagedAttribute);

    return resource;
  }
  
  @Override
  public void delete(UUID id) {
    MetadataManagedAttribute objectStoreMetadata = dao.findOneByNaturalId(id, MetadataManagedAttribute.class);
    if(objectStoreMetadata != null) {
      objectStoreMetadata.setDeletedDate(OffsetDateTime.now());
    }
  }
}
