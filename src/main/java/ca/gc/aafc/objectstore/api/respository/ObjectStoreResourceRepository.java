package ca.gc.aafc.objectstore.api.respository;

import java.util.UUID;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.mapper.ObjectStoreMetaMapperImpl;
import ca.gc.aafc.objectstore.api.mapper.ObjectStoreMetadataMapper;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;

@Repository
@Transactional
public class ObjectStoreResourceRepository
    extends ResourceRepositoryBase<ObjectStoreMetadataDto, UUID> {

  @PersistenceContext
  private EntityManager entityManager;

  //@Inject
  private ObjectStoreMetadataMapper mapper = new ObjectStoreMetaMapperImpl();

  public ObjectStoreResourceRepository() {
    super(ObjectStoreMetadataDto.class);
  }

  /**
   * @param resource
   *          to save
   * @return saved resource
   */
  @Override
  public <S extends ObjectStoreMetadataDto> S save(S resource) {
    ObjectStoreMetadataDto dto =  (ObjectStoreMetadataDto) resource ;
    if(dto.getUuid()==null)
      dto.setUuid(UUID.randomUUID());
    ObjectStoreMetadata objectMetadata = mapper
        .ObjectStoreMetadataDtotoObjectStoreMetadata((ObjectStoreMetadataDto) resource);
    entityManager.persist(objectMetadata);
    return resource;
  }

  @Override
  public ObjectStoreMetadataDto findOne(UUID uuid, QuerySpec querySpec) {
    ObjectStoreMetadata objectStoreMetadata = entityManager.unwrap(Session.class)
        .byNaturalId(ObjectStoreMetadata.class)
        .using("uuid", uuid).load();
    if(objectStoreMetadata ==null)
   // Throw the 404 exception if the resource is not found.
      {
        throw new ResourceNotFoundException(
            this.getClass().getSimpleName() + " with ID " + uuid + " Not Found."
        );
      }
    return mapper.ObjectStoreMetadataToObjectStoreMetadataDto(objectStoreMetadata);
  }

  @Override
  public ResourceList<ObjectStoreMetadataDto> findAll(QuerySpec querySpec) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <S extends ObjectStoreMetadataDto> S create(S resource) {
    return save(resource);
  }
  
  @Override
  public void delete(UUID id) {
    ObjectStoreMetadata objectStoreMetadata = entityManager.unwrap(Session.class)
        .byNaturalId(ObjectStoreMetadata.class)
        .using("uuid", id).load();    
    entityManager.remove(objectStoreMetadata);
  }
}
