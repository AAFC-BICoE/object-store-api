package ca.gc.aafc.objectstore.api.respository;

import java.util.Arrays;

import org.springframework.stereotype.Repository;

import ca.gc.aafc.dina.filter.RsqlFilterHandler;
import ca.gc.aafc.dina.filter.SimpleFilterHandler;
import ca.gc.aafc.dina.repository.JpaDtoRepository;
import ca.gc.aafc.dina.repository.JpaResourceRepository;
import ca.gc.aafc.dina.repository.meta.JpaMetaInformationProvider;
import ca.gc.aafc.objectstore.api.dto.ManagedAttributeDto;

@Repository
public class ManagedAttributeResourceRepository extends JpaResourceRepository<ManagedAttributeDto> {

  public ManagedAttributeResourceRepository(
    JpaDtoRepository dtoRepository,
    SimpleFilterHandler simpleFilterHandler,
    RsqlFilterHandler rsqlFilterHandler,
    JpaMetaInformationProvider metaInformationProvider
  ) {
    super(
      ManagedAttributeDto.class,
      dtoRepository,
      Arrays.asList(simpleFilterHandler, rsqlFilterHandler),
      metaInformationProvider
    );
  }
}
