package ca.gc.aafc.objectstore.api.respository;

import org.springframework.stereotype.Repository;

import ca.gc.aafc.dina.filter.DinaFilterResolver;
import ca.gc.aafc.dina.mapper.DinaMapper;
import ca.gc.aafc.dina.repository.ReadOnlyDinaRepository;
import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.objectstore.api.dto.ObjectUploadDto;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;

@Repository
public class ObjectUploadResourceRepository
    extends ReadOnlyDinaRepository<ObjectUploadDto, ObjectUpload> {

  protected ObjectUploadResourceRepository(DinaService<ObjectUpload> service,
      DinaFilterResolver filterResolver) {
    super(service, new DinaMapper<>(ObjectUploadDto.class), ObjectUploadDto.class, ObjectUpload.class, filterResolver);
  }
}
