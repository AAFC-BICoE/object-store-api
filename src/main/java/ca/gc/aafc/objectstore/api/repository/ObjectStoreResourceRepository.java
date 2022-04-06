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
import lombok.NonNull;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
@Transactional
public class ObjectStoreResourceRepository
  extends DinaRepository<ObjectStoreMetadataDto, ObjectStoreMetadata> {

  private Optional<DinaAuthenticatedUser> authenticatedUser;

  public ObjectStoreResourceRepository(
    @NonNull ObjectStoreMetaDataService dinaService,
    @NonNull ExternalResourceProvider externalResourceProvider,
    Optional<DinaAuthenticatedUser> authenticatedUser,
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
    this.authenticatedUser = authenticatedUser;
  }

  @SuppressWarnings("unchecked")
  @Override
  public ObjectStoreMetadataDto create(ObjectStoreMetadataDto resource) {
    authenticatedUser.ifPresent(
      user -> resource.setCreatedBy(user.getUsername())
    );
    return super.create(resource);
  }

}
