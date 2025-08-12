package ca.gc.aafc.objectstore.api.repository;

import org.springframework.boot.info.BuildProperties;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.gc.aafc.dina.exception.ResourceGoneException;
import ca.gc.aafc.dina.exception.ResourceNotFoundException;
import ca.gc.aafc.dina.jsonapi.JsonApiDocument;
import ca.gc.aafc.dina.mapper.DinaMappingRegistry;
import ca.gc.aafc.dina.repository.DinaRepositoryV2;
import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import ca.gc.aafc.dina.security.auth.DinaAuthorizationService;
import ca.gc.aafc.objectstore.api.dto.DerivativeDto;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.mapper.DerivativeMapper;
import ca.gc.aafc.objectstore.api.service.DerivativeService;

import static com.toedter.spring.hateoas.jsonapi.MediaTypes.JSON_API_VALUE;

import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.validation.ValidationException;
import lombok.NonNull;

@RestController
@RequestMapping(value = "${dina.apiPrefix:}", produces = JSON_API_VALUE)
public class DerivativeRepository extends DinaRepositoryV2<DerivativeDto, Derivative> {

  private final DinaAuthenticatedUser authenticatedUser;

  public DerivativeRepository(
    @NonNull DerivativeService derivativeService,
    DinaAuthorizationService groupAuthorizationService,
    @NonNull BuildProperties buildProperties,
    @NonNull DinaAuthenticatedUser authenticatedUser,
    @NonNull ObjectMapper objMapper
  ) {
    super(
      derivativeService,
      groupAuthorizationService,
      Optional.empty(),
      DerivativeMapper.INSTANCE,
      DerivativeDto.class,
      Derivative.class,
      buildProperties, objMapper, new DinaMappingRegistry(DerivativeDto.class, true));
    this.authenticatedUser = authenticatedUser;
  }

  @GetMapping(DerivativeDto.TYPENAME + "/{id}")
  public ResponseEntity<RepresentationModel<?>> onFindOne(@PathVariable UUID id, HttpServletRequest req)
      throws ResourceNotFoundException, ResourceGoneException {
    return handleFindOne(id, req);
  }

  @GetMapping(DerivativeDto.TYPENAME)
  public ResponseEntity<RepresentationModel<?>> onFindAll(HttpServletRequest req) {
    return handleFindAll(req);
  }

  @PostMapping(DerivativeDto.TYPENAME)
  @Transactional
  public ResponseEntity<RepresentationModel<?>> onCreate(@RequestBody JsonApiDocument postedDocument) {

    return handleCreate(postedDocument, dto -> {
      if (authenticatedUser != null) {
        UUID fileIdentifier = dto.getFileIdentifier();
        // File id required on submission
        if (fileIdentifier == null) {
          throw new ValidationException("fileIdentifier should be provided");
        }
        dto.setCreatedBy(authenticatedUser.getUsername());
      }
    });
  }

  @PatchMapping(DerivativeDto.TYPENAME + "/{id}")
  @Transactional
  public ResponseEntity<RepresentationModel<?>> onUpdate(@RequestBody JsonApiDocument partialPatchDto,
                                                         @PathVariable UUID id) throws ResourceNotFoundException, ResourceGoneException {
    return handleUpdate(partialPatchDto, id);
  }

  @DeleteMapping(DerivativeDto.TYPENAME + "/{id}")
  @Transactional
  public ResponseEntity<RepresentationModel<?>> onDelete(@PathVariable UUID id) throws ResourceNotFoundException, ResourceGoneException {
    return handleDelete(id);
  }

}
