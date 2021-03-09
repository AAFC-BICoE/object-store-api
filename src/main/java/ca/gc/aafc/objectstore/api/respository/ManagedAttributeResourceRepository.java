package ca.gc.aafc.objectstore.api.respository;

import ca.gc.aafc.dina.mapper.DinaMapper;
import ca.gc.aafc.dina.repository.DinaRepository;
import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.objectstore.api.dto.ManagedAttributeDto;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.service.ManagedAttributeAuthorizationService;
import lombok.NonNull;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ManagedAttributeResourceRepository
  extends DinaRepository<ManagedAttributeDto, ManagedAttribute> {

  private final Optional<DinaAuthenticatedUser> authenticatedUser;

  public ManagedAttributeResourceRepository(
    @NonNull DinaService<ManagedAttribute> dinaService,
    @NonNull ManagedAttributeAuthorizationService authorizationService,
    Optional<DinaAuthenticatedUser> authenticatedUser,
    @NonNull BuildProperties props
  ) {
    super(
      dinaService,
      Optional.of(authorizationService),
      Optional.empty(),
      new DinaMapper<>(ManagedAttributeDto.class),
      ManagedAttributeDto.class,
      ManagedAttribute.class, null, null,
      props);
    this.authenticatedUser = authenticatedUser;
  }

  @Override
  public <S extends ManagedAttributeDto> S create(S resource) {
    authenticatedUser.ifPresent(user -> resource.setCreatedBy(user.getUsername()));
    return super.create(resource);
  }

}
