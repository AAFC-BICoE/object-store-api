package ca.gc.aafc.objectstore.api.repository;

import org.springframework.boot.info.BuildProperties;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder;

import ca.gc.aafc.dina.dto.JsonApiDto;
import ca.gc.aafc.dina.exception.ResourceNotFoundException;
import ca.gc.aafc.dina.jsonapi.JsonApiDocument;
import ca.gc.aafc.dina.repository.JsonApiModelAssistant;
import ca.gc.aafc.dina.security.auth.DinaAdminCUDAuthorizationService;
import ca.gc.aafc.objectstore.api.dto.DerivativeDto;
import ca.gc.aafc.objectstore.api.dto.DerivativeGenerationDto;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.service.DerivativeGenerationService;
import ca.gc.aafc.objectstore.api.service.ObjectStoreMetaDataService;

import static com.toedter.spring.hateoas.jsonapi.MediaTypes.JSON_API_VALUE;

import java.net.URI;
import java.util.Optional;
import java.util.Set;
import javax.transaction.Transactional;

/**
 * Administrative repository.
 * DINA_ADMIN role required.
 */
@RestController
@RequestMapping(value = "${dina.apiPrefix:}", produces = JSON_API_VALUE)
public class DerivativeGenerationRepository {

  private final DinaAdminCUDAuthorizationService authorizationService;
  private final ObjectStoreMetaDataService metadataService;
  private final DerivativeGenerationService derivativeGenerationService;

  private final JsonApiModelAssistant<DerivativeGenerationDto> jsonApiModelAssistant;
  private final ObjectMapper objMapper;

  public DerivativeGenerationRepository(DinaAdminCUDAuthorizationService authorizationService,
                                        ObjectStoreMetaDataService metadataService,
                                        DerivativeGenerationService derivativeGenerationService,
                                        BuildProperties buildProperties,
                                        ObjectMapper objMapper) {

    this.authorizationService = authorizationService;
    this.metadataService = metadataService;
    this.derivativeGenerationService = derivativeGenerationService;

    this.objMapper = objMapper;
    this.jsonApiModelAssistant = new JsonApiModelAssistant<>(buildProperties.getVersion());
  }

  @PostMapping(DerivativeGenerationDto.TYPENAME)
  @Transactional
  public ResponseEntity<RepresentationModel<?>> onCreate(@RequestBody JsonApiDocument postedDocument)
    throws ResourceNotFoundException {

    DerivativeGenerationDto dto = this.objMapper.convertValue(postedDocument.getAttributes(), DerivativeGenerationDto.class);

    authorizationService.authorizeCreate(dto);

    if (dto.getDerivativeType() != Derivative.DerivativeType.THUMBNAIL_IMAGE) {
      throw new IllegalArgumentException("DerivativeType can only be THUMBNAIL_IMAGE");
    }

    ObjectStoreMetadata metadata = metadataService.findOne(dto.getMetadataUuid(),
      ObjectStoreMetadata.class, Set.of(ObjectStoreMetadata.DERIVATIVES_PROP));

    if (metadata == null) {
      throw ResourceNotFoundException.create(ObjectStoreMetadataDto.TYPENAME, dto.getMetadataUuid());
    }

    // Check if we have a thumbnail entity already
    Optional<Derivative> existingThumbnail = metadata.getDerivatives().stream()
      .filter(d -> d.getDerivativeType().equals(Derivative.DerivativeType.THUMBNAIL_IMAGE))
      .findFirst();

    // if the thumbnail entity exists, try to fix and return.
    if (existingThumbnail.isPresent()) {
      derivativeGenerationService.fixIncompleteThumbnail(existingThumbnail.get());
      dto.setUuid(existingThumbnail.get().getUuid());
      return createResponse(dto);
    }

    // Check the source to use for the thumbnail
    if (dto.getDerivedFromType() != null) {
      Derivative derivative = metadata.getDerivatives().stream()
        .filter(d -> d.getDerivativeType().equals(dto.getDerivedFromType()))
        .findFirst().orElseThrow(() -> new IllegalStateException("DerivedFromType not found for metadata"));
      derivativeGenerationService.handleThumbnailGeneration(derivative);
    } else {
      derivativeGenerationService.handleThumbnailGeneration(metadata);
    }

    metadataService.refresh(metadata);

    Optional<Derivative> createdThumbnail = metadata.getDerivatives().stream()
      .filter(d -> d.getDerivativeType().equals(Derivative.DerivativeType.THUMBNAIL_IMAGE))
      .findFirst();

    dto.setUuid(createdThumbnail.map(Derivative::getUuid).orElse(null));
    return createResponse(dto);
  }

  private ResponseEntity<RepresentationModel<?>> createResponse(DerivativeGenerationDto dto) {
    JsonApiDto<DerivativeGenerationDto> jsonApiDto = JsonApiDto.<DerivativeGenerationDto>builder()
      .dto(dto).build();
    JsonApiModelBuilder builder = this.jsonApiModelAssistant.createJsonApiModelBuilder(jsonApiDto);
    RepresentationModel<?> model = builder.build();
    // this will be available in data-export using that same uuid
    URI uri = URI.create(DerivativeDto.TYPENAME + "/" + dto.getUuid());
    return ResponseEntity.created(uri).body(model);
  }
}
