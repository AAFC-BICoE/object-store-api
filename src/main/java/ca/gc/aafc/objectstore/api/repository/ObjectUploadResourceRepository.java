package ca.gc.aafc.objectstore.api.repository;

import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.gc.aafc.dina.mapper.DinaMapper;
import ca.gc.aafc.dina.repository.DinaRepository;
import ca.gc.aafc.objectstore.api.dto.ObjectUploadDto;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.security.ObjectUploadAuthorizationService;
import ca.gc.aafc.objectstore.api.service.ObjectUploadService;

import java.util.Optional;
import lombok.NonNull;

// CHECKSTYLE:OFF NoFinalizer
// CHECKSTYLE:OFF SuperFinalize
@Component
public class ObjectUploadResourceRepository
  extends DinaRepository<ObjectUploadDto, ObjectUpload> {

  protected ObjectUploadResourceRepository(
    ObjectUploadService service,
    ObjectUploadAuthorizationService authorizationService,
    BuildProperties props,
    @NonNull ObjectMapper objMapper
  ) {
    super(
      service,
      authorizationService,
      Optional.empty(),
      new DinaMapper<>(ObjectUploadDto.class),
      ObjectUploadDto.class,
      ObjectUpload.class,
      null,
      null,
      props, objMapper);
  }

  /**
   * Protection against CT_CONSTRUCTOR_THROW
   */
  @Override
  protected final void finalize() {
    // no-op
  }
}
