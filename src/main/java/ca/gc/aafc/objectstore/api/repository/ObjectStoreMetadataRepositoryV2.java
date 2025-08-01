package ca.gc.aafc.objectstore.api.repository;

import org.springframework.boot.info.BuildProperties;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.gc.aafc.dina.dto.ExternalRelationDto;
import ca.gc.aafc.dina.dto.JsonApiExternalResource;
import ca.gc.aafc.dina.exception.ResourceGoneException;
import ca.gc.aafc.dina.exception.ResourceNotFoundException;
import ca.gc.aafc.dina.mapper.DinaMappingRegistry;
import ca.gc.aafc.dina.repository.DinaRepositoryV2;
import ca.gc.aafc.dina.service.AuditService;
import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.dto.PersonExternalDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.mapper.ObjectStoreMetadataMapper;
import ca.gc.aafc.objectstore.api.security.MetadataAuthorizationService;

import static com.toedter.spring.hateoas.jsonapi.MediaTypes.JSON_API_VALUE;

import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import lombok.NonNull;

@RestController
@RequestMapping(value = "${dina.apiPrefix:}", produces = JSON_API_VALUE)
public class ObjectStoreMetadataRepositoryV2 extends DinaRepositoryV2<ObjectStoreMetadataDto, ObjectStoreMetadata> {

  public ObjectStoreMetadataRepositoryV2(
    @NonNull DinaService<ObjectStoreMetadata> dinaService,
    @NonNull MetadataAuthorizationService authorizationService,
    @NonNull Optional<AuditService> auditService,
    @NonNull BuildProperties buildProperties,
    ObjectMapper objMapper) {
    super(dinaService, authorizationService, auditService,
      ObjectStoreMetadataMapper.INSTANCE,
      ObjectStoreMetadataDto.class,
      ObjectStoreMetadata.class,
      buildProperties, objMapper,  new DinaMappingRegistry(ObjectStoreMetadataDto.class, true));
  }

  @Override
  protected JsonApiExternalResource externalRelationDtoToJsonApiExternalResource(
    ExternalRelationDto externalRelationDto) {

    if (externalRelationDto == null) {
      return null;
    }

    if (PersonExternalDto.EXTERNAL_TYPENAME.equals(externalRelationDto.getType())) {
      return PersonExternalDto.builder().uuid(UUID.fromString(externalRelationDto.getId())).build();
    }
    return null;
  }

  @GetMapping(ObjectStoreMetadataDto.TYPENAME + "/{id}")
  public ResponseEntity<RepresentationModel<?>> onFindOne(@PathVariable UUID id, HttpServletRequest req)
      throws ResourceNotFoundException, ResourceGoneException {
    return handleFindOne(id, req);
  }

  @GetMapping(ObjectStoreMetadataDto.TYPENAME)
  public ResponseEntity<RepresentationModel<?>> onFindAll(HttpServletRequest req) {
    return handleFindAll(req);
  }

}
