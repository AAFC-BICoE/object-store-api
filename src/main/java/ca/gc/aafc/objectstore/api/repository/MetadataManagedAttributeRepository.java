package ca.gc.aafc.objectstore.api.repository;

import ca.gc.aafc.dina.mapper.DinaMapper;
import ca.gc.aafc.dina.repository.DinaRepository;
import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.objectstore.api.dto.MetadataManagedAttributeDto;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import lombok.NonNull;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MetadataManagedAttributeRepository
  extends DinaRepository<MetadataManagedAttributeDto, MetadataManagedAttribute> {

  private final DinaAuthenticatedUser authenticatedUser;

  public MetadataManagedAttributeRepository(
    @NonNull DinaService<MetadataManagedAttribute> dinaService,
    @NonNull DinaAuthenticatedUser authenticatedUser,
    @NonNull BuildProperties props
  ) {
    super(
      dinaService,
      Optional.empty(),
      Optional.empty(),
      new DinaMapper<>(MetadataManagedAttributeDto.class),
      MetadataManagedAttributeDto.class,
      MetadataManagedAttribute.class,
      null,
      null,
      props);
    this.authenticatedUser = authenticatedUser;
  }

  @Override
  public <S extends MetadataManagedAttributeDto> S create(S resource) {
    resource.setCreatedBy(authenticatedUser.getUsername());
    return super.create(resource);
  }
}
