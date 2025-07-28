package ca.gc.aafc.objectstore.api.repository;

import org.springframework.boot.info.BuildProperties;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.gc.aafc.dina.exception.ResourceGoneException;
import ca.gc.aafc.dina.exception.ResourceNotFoundException;
import ca.gc.aafc.dina.jsonapi.JsonApiDocument;
import ca.gc.aafc.dina.repository.DinaRepositoryV2;
import ca.gc.aafc.dina.service.AuditService;
import ca.gc.aafc.objectstore.api.dto.ObjectUploadDto;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.mapper.ObjectUploadMapper;
import ca.gc.aafc.objectstore.api.security.ObjectUploadAuthorizationService;
import ca.gc.aafc.objectstore.api.service.ObjectUploadService;

import static com.toedter.spring.hateoas.jsonapi.MediaTypes.JSON_API_VALUE;

import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import lombok.NonNull;

@RestController
@RequestMapping(value = "${dina.apiPrefix:}", produces = JSON_API_VALUE)
public class ObjectUploadResourceRepository extends DinaRepositoryV2<ObjectUploadDto, ObjectUpload> {

  public ObjectUploadResourceRepository(
    @NonNull ObjectUploadService dinaService,
    @NonNull ObjectUploadAuthorizationService authorizationService,
    Optional<AuditService> auditService,
    @NonNull BuildProperties props,
    @NonNull ObjectMapper objMapper
  ) {
    super(
      dinaService, authorizationService,
      auditService,
      ObjectUploadMapper.INSTANCE,
      ObjectUploadDto.class,
      ObjectUpload.class,
      props, objMapper);
  }

  @GetMapping(ObjectUploadDto.TYPENAME + "/{id}")
  public ResponseEntity<RepresentationModel<?>> onFindOne(@PathVariable UUID id,
                                                          HttpServletRequest req)
    throws ResourceNotFoundException, ResourceGoneException {

    return handleFindOne(id, req);
  }

  @GetMapping(ObjectUploadDto.TYPENAME)
  public ResponseEntity<RepresentationModel<?>> onFindAll(HttpServletRequest req) {
    return handleFindAll(req);
  }


  @PatchMapping(ObjectUploadDto.TYPENAME + "/{id}")
  @Transactional
  public ResponseEntity<RepresentationModel<?>> onUpdate(@RequestBody
                                                         JsonApiDocument partialPatchDto,
                                                         @PathVariable UUID id) throws ResourceNotFoundException, ResourceGoneException {
    return handleUpdate(partialPatchDto, id);
  }

  @DeleteMapping(ObjectUploadDto.TYPENAME + "/{id}")
  @Transactional
  public ResponseEntity<RepresentationModel<?>> onDelete(@PathVariable UUID id) throws ResourceNotFoundException, ResourceGoneException {
    return handleDelete(id);
  }
}
