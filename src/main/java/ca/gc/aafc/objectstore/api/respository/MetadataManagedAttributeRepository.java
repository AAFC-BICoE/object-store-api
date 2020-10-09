package ca.gc.aafc.objectstore.api.respository;

import ca.gc.aafc.dina.filter.DinaFilterResolver;
import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.mapper.DinaMapper;
import ca.gc.aafc.dina.repository.DinaRepository;
import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.objectstore.api.dto.MetadataManagedAttributeDto;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import lombok.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MetadataManagedAttributeRepository
  extends DinaRepository<MetadataManagedAttributeDto, MetadataManagedAttribute> {

  private final DinaAuthenticatedUser authenticatedUser;

  public MetadataManagedAttributeRepository(
    @NonNull BaseDAO baseDAO,
    @NonNull DinaFilterResolver filterResolver,
    @NonNull DinaAuthenticatedUser authenticatedUser
  ) {
    super(
      new DinaService<>(baseDAO),
      Optional.empty(),
      Optional.empty(),
      new DinaMapper<>(MetadataManagedAttributeDto.class),
      MetadataManagedAttributeDto.class,
      MetadataManagedAttribute.class,
      filterResolver,
      null);
    this.authenticatedUser = authenticatedUser;
  }

  @Override
  public <S extends MetadataManagedAttributeDto> S create(S resource) {
    resource.setCreatedBy(authenticatedUser.getUsername());
    return super.create(resource);
  }
}
