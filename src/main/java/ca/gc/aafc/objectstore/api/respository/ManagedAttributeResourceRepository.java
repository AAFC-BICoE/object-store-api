package ca.gc.aafc.objectstore.api.respository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import ca.gc.aafc.objectstore.api.dto.ManagedAttributeDto;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.mapper.ManagedAttributeMapper;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.data.jpa.query.criteria.JpaCriteriaQuery;
import io.crnk.data.jpa.query.criteria.JpaCriteriaQueryFactory;

@Repository
@Transactional
public class ManagedAttributeResourceRepository extends ResourceRepositoryBase<ManagedAttributeDto, UUID> {

  @PersistenceContext
  private EntityManager entityManager;

  @Inject
  private ManagedAttributeMapper mapper;
  
  private JpaCriteriaQueryFactory queryFactory;  

  public ManagedAttributeResourceRepository() {
    super(ManagedAttributeDto.class);
  }


  
  @PostConstruct
  void setup() {
    queryFactory = JpaCriteriaQueryFactory.newInstance(entityManager);
  }
  
  private ManagedAttribute findOneByUUID(UUID uuid) {

    ManagedAttribute managedAttribute = entityManager.unwrap(Session.class)
        .byNaturalId(ManagedAttribute.class).using("uuid", uuid).load();
    return managedAttribute;

  }

  /**
   * @param resource
   *          to save
   * @return saved resource
   */
  @Override
  public <S extends ManagedAttributeDto> S save(S resource) {
    ManagedAttributeDto dto = (ManagedAttributeDto) resource;
    ManagedAttribute managedAttribute = findOneByUUID(dto.getUuid());
    mapper.updateManagedAttributeFromDto(dto, managedAttribute);
    entityManager.merge(managedAttribute);
    return resource;
  }

  @Override
  public ManagedAttributeDto findOne(UUID uuid, QuerySpec querySpec) {
    ManagedAttribute managedAttribute = findOneByUUID(uuid);
    if (managedAttribute == null) {
      // Throw the 404 exception if the resource is not found.
      throw new ResourceNotFoundException(
          this.getClass().getSimpleName() + " with ID " + uuid + " Not Found.");
    }
    return mapper.toDto(managedAttribute);
  }

  @Override
  public ResourceList<ManagedAttributeDto> findAll(QuerySpec querySpec) {
    JpaCriteriaQuery<ManagedAttribute> jq = queryFactory.query(ManagedAttribute.class);
    
    List<ManagedAttributeDto> l = jq.buildExecutor(querySpec).getResultList().stream()
    .map(mapper::toDto)
    .collect(Collectors.toList());
    
    return new DefaultResourceList<ManagedAttributeDto>(l, null, null);
  }
  
  public ResourceList<ManagedAttributeDto> findAll(Iterable<Serializable> ids) {
    @SuppressWarnings("unchecked")
    List<ManagedAttributeDto> managedAttributes = new ArrayList<ManagedAttributeDto>();
    Iterator it = ids.iterator();
    while(it.hasNext()) {
      managedAttributes.add(findOne((UUID)it.next(),null));      
    }
    return new DefaultResourceList<ManagedAttributeDto>(managedAttributes, null, null);
  }  

  @Override
  public <S extends ManagedAttributeDto> S create(S resource) {
    ManagedAttributeDto dto = (ManagedAttributeDto) resource;
    if (dto.getUuid() == null) {
      dto.setUuid(UUID.randomUUID());
    }
    ManagedAttribute managedAttribute = mapper.toEntity((ManagedAttributeDto) resource);
    entityManager.persist(managedAttribute);
    return resource;
  }

  @Override
  public void delete(UUID id) {
    ManagedAttribute managedAttribute = findOneByUUID(id);
    if (managedAttribute != null) {
      entityManager.remove(managedAttribute);
    }
  }
}
