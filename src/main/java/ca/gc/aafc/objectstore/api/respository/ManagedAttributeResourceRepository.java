package ca.gc.aafc.objectstore.api.respository;

import ca.gc.aafc.dina.entity.DinaEntity;
import ca.gc.aafc.dina.filter.DinaFilterResolver;
import ca.gc.aafc.dina.mapper.DinaMapper;
import ca.gc.aafc.dina.repository.DinaRepository;
import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.objectstore.api.dto.ManagedAttributeDto;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import ca.gc.aafc.objectstore.api.service.ManagedAttributeAuthorizationService;
import io.crnk.core.exception.ResourceNotFoundException;
import lombok.NonNull;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;

import javax.persistence.criteria.Predicate;
import java.io.Serializable;
import java.util.Optional;

@Repository
public class ManagedAttributeResourceRepository
  extends DinaRepository<ManagedAttributeDto, ManagedAttribute> {

  private final Optional<DinaAuthenticatedUser> authenticatedUser;
  private final DinaService<ManagedAttribute> dinaService;

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
    this.dinaService = dinaService;
  }

  @Override
  public <S extends ManagedAttributeDto> S create(S resource) {
    authenticatedUser.ifPresent(user -> resource.setCreatedBy(user.getUsername()));
    return super.create(resource);
  }

  @Override
  public void delete(Serializable id) {
    ManagedAttribute entity = this.dinaService.findOne(id, ManagedAttribute.class);
    if (entity == null) {
      throw new ResourceNotFoundException("ManagedAttribute with id " + id + " not found.");
    }

    int size = dinaService.findAll(
      MetadataManagedAttribute.class,
      (cb, root) -> new Predicate[]{cb.equal(root.get("managedAttribute"), entity)}, null, 0, 1000).size();
    if (size > 0) {

    }

    super.delete(id);
  }
}
