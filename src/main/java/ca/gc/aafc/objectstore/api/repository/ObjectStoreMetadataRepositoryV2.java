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

import ca.gc.aafc.dina.dto.ExternalRelationDto;
import ca.gc.aafc.dina.dto.JsonApiExternalResource;
import ca.gc.aafc.dina.exception.ResourceGoneException;
import ca.gc.aafc.dina.exception.ResourceNotFoundException;
import ca.gc.aafc.dina.json.JsonDocumentInspector;
import ca.gc.aafc.dina.jsonapi.JsonApiDocument;
import ca.gc.aafc.dina.mapper.DinaMappingRegistry;
import ca.gc.aafc.dina.repository.DinaRepositoryV2;
import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import ca.gc.aafc.dina.security.TextHtmlSanitizer;
import ca.gc.aafc.dina.service.AuditService;
import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.dto.PersonExternalDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.mapper.ObjectStoreMetadataMapper;
import ca.gc.aafc.objectstore.api.security.MetadataAuthorizationService;

import static com.toedter.spring.hateoas.jsonapi.MediaTypes.JSON_API_VALUE;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import lombok.NonNull;

@RestController
@RequestMapping(value = "${dina.apiPrefix:}", produces = JSON_API_VALUE)
public class ObjectStoreMetadataRepositoryV2 extends DinaRepositoryV2<ObjectStoreMetadataDto, ObjectStoreMetadata> {

  private final DinaAuthenticatedUser authenticatedUser;

  public ObjectStoreMetadataRepositoryV2(
    @NonNull DinaService<ObjectStoreMetadata> dinaService,
    @NonNull MetadataAuthorizationService authorizationService,
    Optional<DinaAuthenticatedUser> authenticatedUser,
    @NonNull Optional<AuditService> auditService,
    @NonNull BuildProperties buildProperties,
    ObjectMapper objMapper) {
    super(dinaService, authorizationService, auditService,
      ObjectStoreMetadataMapper.INSTANCE,
      ObjectStoreMetadataDto.class,
      ObjectStoreMetadata.class,
      buildProperties, objMapper,  new DinaMappingRegistry(ObjectStoreMetadataDto.class, true));
    this.authenticatedUser = authenticatedUser.orElse(null);
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

  @PostMapping(ObjectStoreMetadataDto.TYPENAME)
  @Transactional
  public ResponseEntity<RepresentationModel<?>> onCreate(@RequestBody JsonApiDocument postedDocument) {

    return handleCreate(postedDocument, dto -> {
      if (authenticatedUser != null) {
        dto.setCreatedBy(authenticatedUser.getUsername());
      }
    });
  }

  @PatchMapping(ObjectStoreMetadataDto.TYPENAME + "/{id}")
  @Transactional
  public ResponseEntity<RepresentationModel<?>> onUpdate(@RequestBody JsonApiDocument partialPatchDto,
                                                         @PathVariable UUID id) throws ResourceNotFoundException, ResourceGoneException {
    return handleUpdate(partialPatchDto, id);
  }

  @DeleteMapping(ObjectStoreMetadataDto.TYPENAME + "/{id}")
  @Transactional
  public ResponseEntity<RepresentationModel<?>> onDelete(@PathVariable UUID id) throws ResourceNotFoundException, ResourceGoneException {
    return handleDelete(id);
  }

  @Override
  protected void checkSubmittedData(Map<String, Object> attributes) {
    Map<String, Object> attributesToCheck = new HashMap<>(attributes);

    //remove managedAttributes to allow OCR data
    attributesToCheck.remove("managedAttributes");

    if (!JsonDocumentInspector.testPredicateOnValues(attributesToCheck, supplyCheckSubmittedDataPredicate())) {
      throw new IllegalArgumentException("Unaccepted value detected in attributes");
    }
  }

  @Override
  protected Predicate<String> supplyCheckSubmittedDataPredicate() {
    return txt -> TextHtmlSanitizer.isSafeText(txt) || TextHtmlSanitizer.isAcceptableText(txt);
  }

}
