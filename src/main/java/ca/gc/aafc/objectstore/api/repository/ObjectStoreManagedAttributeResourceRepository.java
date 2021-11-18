package ca.gc.aafc.objectstore.api.repository;

import ca.gc.aafc.dina.mapper.DinaMapper;
import ca.gc.aafc.dina.repository.DinaRepository;
import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreManagedAttributeDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreManagedAttribute;
import ca.gc.aafc.objectstore.api.service.ObjectStoreManagedAttributeAuthorizationService;
import lombok.NonNull;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ObjectStoreManagedAttributeResourceRepository
  extends DinaRepository<ObjectStoreManagedAttributeDto, ObjectStoreManagedAttribute> {

  private final Optional<DinaAuthenticatedUser> authenticatedUser;

  public ObjectStoreManagedAttributeResourceRepository(
    @NonNull DinaService<ObjectStoreManagedAttribute> dinaService,
    @NonNull ObjectStoreManagedAttributeAuthorizationService authorizationService,
    Optional<DinaAuthenticatedUser> authenticatedUser,
    @NonNull BuildProperties props
  ) {
    super(
      dinaService,
      authorizationService,
      Optional.empty(),
      new DinaMapper<>(ObjectStoreManagedAttributeDto.class),
      ObjectStoreManagedAttributeDto.class,
      ObjectStoreManagedAttribute.class, null, null,
      props);
    this.authenticatedUser = authenticatedUser;
  }

  @Override
  public <S extends ObjectStoreManagedAttributeDto> S create(S resource) {
    authenticatedUser.ifPresent(user -> resource.setCreatedBy(user.getUsername()));
    return super.create(resource);
  }

}
