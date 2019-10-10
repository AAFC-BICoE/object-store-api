package ca.gc.aafc.objectstore.api.respository;

import java.util.UUID;

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
import io.crnk.core.resource.list.ResourceList;

@Repository
@Transactional
public class ManagedAttributeResourceRepository extends ResourceRepositoryBase<ManagedAttributeDto, UUID> {

  @PersistenceContext
  private EntityManager entityManager;

  @Inject
  private ManagedAttributeMapper mapper;

  public ManagedAttributeResourceRepository() {
    super(ManagedAttributeDto.class);
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
    ManagedAttribute mappedManagedAttribute = mapper.toEntity((ManagedAttributeDto) resource);

    managedAttribute.setManagedAttributeType(mappedManagedAttribute.getManagedAttributeType());
    managedAttribute.setName(mappedManagedAttribute.getName());
    managedAttribute.setAcceptedValues(mappedManagedAttribute.getAcceptedValues());
    
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
    // TODO Auto-generated method stub
    return null;
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
