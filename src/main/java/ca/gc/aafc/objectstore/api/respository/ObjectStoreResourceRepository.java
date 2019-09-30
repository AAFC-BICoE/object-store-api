package ca.gc.aafc.objectstore.api.respository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.mapper.ObjectStoreMetadataMapper;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;


@SuppressWarnings("rawtypes")
@Component
public class ObjectStoreResourceRepository extends ResourceRepositoryBase<ObjectStoreMetadataDto, Integer>{
  
  
  @PersistenceContext
  private EntityManager entityManager;
  
  @Override
  public Class<ObjectStoreMetadataDto> getResourceClass() {
    return ObjectStoreMetadataDto.class;
  }
  
  private static final ObjectStoreMetadataMapper DTO_MAPPER = Mappers
      .getMapper(ObjectStoreMetadataMapper.class);  
    
   /**
   * @param resource to save
   * @return saved resource
   */
  @SuppressWarnings("unchecked")
  @Override
  public <S extends ObjectStoreMetadataDto> S save(S resource) {
    ObjectStoreMetadata objectMetadata = DTO_MAPPER.destinationToSource((ObjectStoreMetadataDto) resource);
    entityManager.persist(objectMetadata);
    entityManager.flush();
    entityManager.detach(objectMetadata);       
    return resource;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public ObjectStoreMetadataDto findOne(Integer id, QuerySpec querySpec) {
    ObjectStoreMetadata objectStoreMetadata  = entityManager.find(ObjectStoreMetadata.class,(Integer)id);
    return DTO_MAPPER.sourceToDestination(objectStoreMetadata);
  }

  @Override
  public ResourceList<ObjectStoreMetadataDto> findAll(QuerySpec querySpec) {
    // TODO Auto-generated method stub
    return null;
  }

  public ObjectStoreResourceRepository() {
    super(ObjectStoreMetadataDto.class);
    
  }
 }
