package ca.gc.aafc.objectstore.api.respository;

import java.util.Arrays;

import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;

import ca.gc.aafc.dina.filter.RsqlFilterHandler;
import ca.gc.aafc.dina.filter.SimpleFilterHandler;
import ca.gc.aafc.dina.repository.JpaDtoRepository;
import ca.gc.aafc.dina.repository.JpaResourceRepository;
import ca.gc.aafc.dina.repository.meta.JpaMetaInformationProvider;
import ca.gc.aafc.objectstore.api.dto.ObjectSubtypeDto;

@Repository
@Transactional
public class ObjectSubtypeResourceRepository extends JpaResourceRepository<ObjectSubtypeDto> {

  public ObjectSubtypeResourceRepository(
    JpaDtoRepository dtoRepository,
    SimpleFilterHandler simpleFilterHandler,
    RsqlFilterHandler rsqlFilterHandler,
    JpaMetaInformationProvider metaInformationProvider
  ) {
    super(
      ObjectSubtypeDto.class,
      dtoRepository,
      Arrays.asList(simpleFilterHandler, rsqlFilterHandler),
      metaInformationProvider
    );
  }

}
