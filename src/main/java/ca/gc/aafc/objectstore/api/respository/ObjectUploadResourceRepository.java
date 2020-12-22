package ca.gc.aafc.objectstore.api.respository;

import ca.gc.aafc.dina.filter.DinaFilterResolver;
import ca.gc.aafc.dina.mapper.DinaMapper;
import ca.gc.aafc.dina.repository.ReadOnlyDinaRepository;
import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.objectstore.api.dto.ObjectUploadDto;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

@Component
public class ObjectUploadResourceRepository
  extends ReadOnlyDinaRepository<ObjectUploadDto, ObjectUpload> {

  protected ObjectUploadResourceRepository(
    DinaService<ObjectUpload> service,
    BuildProperties props,
    DinaFilterResolver filterResolver
  ) {
    super(
      service,
      new DinaMapper<>(ObjectUploadDto.class),
      ObjectUploadDto.class,
      ObjectUpload.class,
      filterResolver,
      props);
  }
}
