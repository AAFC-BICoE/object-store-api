package ca.gc.aafc.objectstore.api.repository;

import org.springframework.boot.info.BuildProperties;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder;

import ca.gc.aafc.dina.dto.ExternalRelationDto;
import ca.gc.aafc.dina.dto.JsonApiDto;
import ca.gc.aafc.dina.dto.JsonApiExternalResource;
import ca.gc.aafc.dina.exception.ResourceNotFoundException;
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
@RequestMapping(value = "/api/v1", produces = JSON_API_VALUE)
public class ObjectStoreMetadataRepositoryV2 extends DinaRepositoryV2<ObjectStoreMetadataDto, ObjectStoreMetadata> {

  private static final String TMP_V2_TYPE = ObjectStoreMetadataDto.TYPENAME + "2";

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
      buildProperties, objMapper);
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

  @GetMapping(TMP_V2_TYPE + "/{id}")
  public ResponseEntity<RepresentationModel<?>> handleFindOne(@PathVariable UUID id, HttpServletRequest req) throws ResourceNotFoundException {
    String queryString = decodeQueryString(req);

    JsonApiDto<ObjectStoreMetadataDto> jsonApiDto = getOne(id, queryString);
    if (jsonApiDto == null) {
      return ResponseEntity.notFound().build();
    }

    JsonApiModelBuilder builder = createJsonApiModelBuilder(jsonApiDto);

    return ResponseEntity.ok(builder.build());
  }

  @GetMapping(TMP_V2_TYPE)
  public ResponseEntity<RepresentationModel<?>> handleFindAll(HttpServletRequest req) {
    String queryString = decodeQueryString(req);

    PagedResource<JsonApiDto<ObjectStoreMetadataDto>> dtos;
    try {
      dtos = getAll(queryString);
    } catch (IllegalArgumentException iaEx) {
      return ResponseEntity.badRequest().build();
    }

    JsonApiModelBuilder builder = createJsonApiModelBuilder(dtos);

    return ResponseEntity.ok(builder.build());
  }

}
