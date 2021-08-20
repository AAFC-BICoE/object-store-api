package ca.gc.aafc.objectstore.api.repository;

import ca.gc.aafc.dina.mapper.DinaMapper;
import ca.gc.aafc.dina.repository.ReadOnlyDinaRepository;
import ca.gc.aafc.objectstore.api.dto.ObjectUploadDto;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.service.ObjectUploadService;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

@Component
public class ObjectUploadResourceRepository
  extends ReadOnlyDinaRepository<ObjectUploadDto, ObjectUpload> {

  protected ObjectUploadResourceRepository(
    ObjectUploadService service,
    BuildProperties props
  ) {
    super(
      service,
      new DinaMapper<>(ObjectUploadDto.class),
      ObjectUploadDto.class,
      ObjectUpload.class,
      null,
      props);
  }
}
