package ca.gc.aafc.objectstore.api.repository;

import org.springframework.boot.info.BuildProperties;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.gc.aafc.dina.exception.ResourceGoneException;
import ca.gc.aafc.dina.exception.ResourceNotFoundException;
import ca.gc.aafc.dina.jsonapi.JsonApiDocument;
import ca.gc.aafc.dina.repository.DinaRepositoryV2;
import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import ca.gc.aafc.dina.security.TextHtmlSanitizer;
import ca.gc.aafc.dina.service.AuditService;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreManagedAttributeDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreManagedAttribute;
import ca.gc.aafc.objectstore.api.mapper.ObjectStoreManagedAttributeMapper;
import ca.gc.aafc.objectstore.api.security.ObjectStoreManagedAttributeAuthorizationService;
import ca.gc.aafc.objectstore.api.service.ObjectStoreManagedAttributeService;

import static com.toedter.spring.hateoas.jsonapi.MediaTypes.JSON_API_VALUE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import lombok.NonNull;

@RestController
@RequestMapping(value = "${dina.apiPrefix:}", produces = JSON_API_VALUE)
public class ObjectStoreManagedAttributeResourceRepository
  extends DinaRepositoryV2<ObjectStoreManagedAttributeDto, ObjectStoreManagedAttribute> {

  private final ObjectStoreManagedAttributeService dinaService;
  private final DinaAuthenticatedUser authenticatedUser;

  public ObjectStoreManagedAttributeResourceRepository(
    @NonNull ObjectStoreManagedAttributeService dinaService,
    @NonNull ObjectStoreManagedAttributeAuthorizationService authorizationService,
    Optional<DinaAuthenticatedUser> authenticatedUser,
    Optional<AuditService> auditService,
    @NonNull BuildProperties props,
    @NonNull ObjectMapper objMapper
  ) {
    super(
      dinaService, authorizationService,
      auditService,
      ObjectStoreManagedAttributeMapper.INSTANCE,
      ObjectStoreManagedAttributeDto.class,
      ObjectStoreManagedAttribute.class,
      props, objMapper);
    this.authenticatedUser = authenticatedUser.orElse(null);
    this.dinaService = dinaService;
  }

  @Override
  protected Link generateLinkToResource(ObjectStoreManagedAttributeDto dto) {
    try {
      return linkTo(methodOn(ObjectStoreManagedAttributeResourceRepository.class).onFindOne(dto.getUuid().toString(), null)).withSelfRel();
    } catch (ResourceNotFoundException | ResourceGoneException e) {
      throw new RuntimeException(e);
    }
  }

  @PostMapping(ObjectStoreManagedAttributeDto.TYPENAME)
  @Transactional
  public ResponseEntity<RepresentationModel<?>> onCreate(@RequestBody JsonApiDocument postedDocument) {

    return handleCreate(postedDocument, dto -> {
      if (authenticatedUser != null) {
        dto.setCreatedBy(authenticatedUser.getUsername());
      }
    });
  }

  @GetMapping(ObjectStoreManagedAttributeDto.TYPENAME + "/{id}")
  public ResponseEntity<RepresentationModel<?>> onFindOne(@PathVariable String id,
                                                          HttpServletRequest req)
      throws ResourceNotFoundException, ResourceGoneException {

    boolean idIsUuid = true;
    try {
      UUID.fromString(id);
    } catch (IllegalArgumentException exception) {
      idIsUuid = false;
    }

    // Try use UUID
    if (idIsUuid) {
      return handleFindOne(UUID.fromString(id), req);
    }

    ObjectStoreManagedAttribute managedAttribute = dinaService.findOneByKey(id);
    if (managedAttribute != null) {
      return handleFindOne(managedAttribute.getUuid(), req);
    } else {
      throw ResourceNotFoundException.create(ObjectStoreManagedAttributeDto.TYPENAME,
        TextHtmlSanitizer.sanitizeText(id));
    }

  }

  @GetMapping(ObjectStoreManagedAttributeDto.TYPENAME)
  public ResponseEntity<RepresentationModel<?>> onFindAll(HttpServletRequest req) {
    return handleFindAll(req);
  }

  @PatchMapping(ObjectStoreManagedAttributeDto.TYPENAME + "/{id}")
  @Transactional
  public ResponseEntity<RepresentationModel<?>> onUpdate(@RequestBody JsonApiDocument partialPatchDto,
                                                         @PathVariable UUID id) throws ResourceNotFoundException, ResourceGoneException {
    return handleUpdate(partialPatchDto, id);
  }

  @DeleteMapping(ObjectStoreManagedAttributeDto.TYPENAME + "/{id}")
  @Transactional
  public ResponseEntity<RepresentationModel<?>> onDelete(@PathVariable UUID id) throws ResourceNotFoundException, ResourceGoneException {
    return handleDelete(id);
  }
}
