package ca.gc.aafc.objectstore.api.respository;

import ca.gc.aafc.dina.filter.RsqlFilterHandler;
import ca.gc.aafc.dina.filter.SimpleFilterHandler;
import ca.gc.aafc.dina.repository.JpaDtoRepository;
import ca.gc.aafc.dina.repository.JpaResourceRepository;
import ca.gc.aafc.dina.repository.meta.JpaMetaInformationProvider;
import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import ca.gc.aafc.objectstore.api.dto.MetadataManagedAttributeDto;
import org.springframework.stereotype.Repository;

import java.util.Arrays;

@Repository
public class MetadataManagedAttributeRepository extends JpaResourceRepository<MetadataManagedAttributeDto> {

  private final DinaAuthenticatedUser authenticatedUser;

  public MetadataManagedAttributeRepository(
    JpaDtoRepository dtoRepository,
    SimpleFilterHandler simpleFilterHandler,
    RsqlFilterHandler rsqlFilterHandler,
    JpaMetaInformationProvider metaInformationProvider,
    DinaAuthenticatedUser authenticatedUser
  ) {
    super(
      MetadataManagedAttributeDto.class,
      dtoRepository,
      Arrays.asList(simpleFilterHandler, rsqlFilterHandler),
      metaInformationProvider
    );
    this.authenticatedUser = authenticatedUser;
  }

  @Override
  public <S extends MetadataManagedAttributeDto> S create(S resource) {
    resource.setCreatedBy(authenticatedUser.getUsername());
    return super.create(resource);
  }
}
