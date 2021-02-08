package ca.gc.aafc.objectstore.api.respository;

import ca.gc.aafc.dina.filter.DinaFilterResolver;
import ca.gc.aafc.dina.mapper.DinaMapper;
import ca.gc.aafc.dina.repository.DinaRepository;
import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.objectstore.api.dto.ManagedAttributeDto;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import ca.gc.aafc.objectstore.api.exceptionmapping.ManagedAttributeChildConflictException;
import ca.gc.aafc.objectstore.api.service.ManagedAttributeAuthorizationService;
import io.crnk.core.exception.ResourceNotFoundException;
import lombok.NonNull;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.Predicate;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ManagedAttributeResourceRepository
  extends DinaRepository<ManagedAttributeDto, ManagedAttribute> {

  private final Optional<DinaAuthenticatedUser> authenticatedUser;

  public ManagedAttributeResourceRepository(
    @NonNull DinaService<ManagedAttribute> dinaService,
    @NonNull DinaFilterResolver filterResolver,
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
      ManagedAttribute.class, filterResolver, null,
      props);
    this.authenticatedUser = authenticatedUser;
  }

  @Override
  public <S extends ManagedAttributeDto> S create(S resource) {
    authenticatedUser.ifPresent(user -> resource.setCreatedBy(user.getUsername()));
    return super.create(resource);
  }

}
