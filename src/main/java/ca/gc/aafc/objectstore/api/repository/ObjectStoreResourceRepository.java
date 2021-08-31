package ca.gc.aafc.objectstore.api.repository;

import ca.gc.aafc.dina.mapper.DinaMapper;
import ca.gc.aafc.dina.repository.DinaRepository;
import ca.gc.aafc.dina.repository.external.ExternalResourceProvider;
import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import ca.gc.aafc.dina.service.AuditService;
import ca.gc.aafc.dina.security.DinaAuthorizationService;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.service.ObjectStoreMetaDataService;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.Optional;

@Log4j2
@Repository
@Transactional
public class ObjectStoreResourceRepository
  extends DinaRepository<ObjectStoreMetadataDto, ObjectStoreMetadata> {

  private final ObjectStoreMetaDataService dinaService;
  private final DinaAuthenticatedUser authenticatedUser;

  public ObjectStoreResourceRepository(
    @NonNull ObjectStoreMetaDataService dinaService,
    @NonNull ExternalResourceProvider externalResourceProvider,
    @NonNull DinaAuthenticatedUser authenticatedUser,
    @NonNull AuditService auditService,
    DinaAuthorizationService groupAuthorizationService,
    @NonNull BuildProperties props
  ) {
    super(
      dinaService,
      groupAuthorizationService,
      Optional.of(auditService),
      new DinaMapper<>(ObjectStoreMetadataDto.class),
      ObjectStoreMetadataDto.class,
      ObjectStoreMetadata.class,
      null,
      externalResourceProvider,
      props);
    this.dinaService = dinaService;
    this.authenticatedUser = authenticatedUser;
  }

  /**
   * @param resource to save
   * @return saved resource
   */
  @Override
  @SuppressWarnings("unchecked")
  public <S extends ObjectStoreMetadataDto> S save(S resource) {
    S dto = super.save(resource);
    return (S) this.findOne(dto.getUuid(), new QuerySpec(ObjectStoreMetadataDto.class));
  }

  @Override
  public ObjectStoreMetadataDto findOne(Serializable id, QuerySpec querySpec) {
    // Omit "managedAttributeMap" from the JPA include spec, because it is a generated object, not on the JPA model.
    QuerySpec jpaFriendlyQuerySpec = querySpec.clone();
    jpaFriendlyQuerySpec.getIncludedRelations()
      .removeIf(include -> include.getPath().toString().equals("managedAttributeMap"));

    return super.findOne(id, jpaFriendlyQuerySpec);
  }

  @Override
  public ResourceList<ObjectStoreMetadataDto> findAll(QuerySpec querySpec) {
    // Omit "managedAttributeMap" from the JPA include spec, because it is a generated object, not on the JPA model.
    QuerySpec jpaFriendlyQuerySpec = querySpec.clone();
    jpaFriendlyQuerySpec.getIncludedRelations()
      .removeIf(include -> include.getPath().toString().equals("managedAttributeMap"));

    return super.findAll(jpaFriendlyQuerySpec);
  }

  @SuppressWarnings("unchecked")
  @Override
  public ObjectStoreMetadataDto create(ObjectStoreMetadataDto resource) {

    resource.setCreatedBy(authenticatedUser.getUsername());
    ObjectStoreMetadataDto created = super.create(resource);

    return this.findOne(
      created.getUuid(),
      new QuerySpec(ObjectStoreMetadataDto.class)
    );
  }

}
