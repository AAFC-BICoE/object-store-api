package ca.gc.aafc.objectstore.api.repository;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.toedter.spring.hateoas.jsonapi.JsonApiError;
import com.toedter.spring.hateoas.jsonapi.JsonApiErrors;
import com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder;

import ca.gc.aafc.dina.repository.ResourceNameIdentifierBaseRepository;
import ca.gc.aafc.dina.security.auth.GroupWithReadAuthorizationService;
import ca.gc.aafc.dina.service.NameUUIDPair;
import ca.gc.aafc.dina.service.ResourceNameIdentifierService;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.dto.ResourceNameIdentifierResponseDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;

import static com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder.jsonApiModel;
import static com.toedter.spring.hateoas.jsonapi.MediaTypes.JSON_API_VALUE;

/**
 * Endpoint used to get identifiers (uuid) based on name.
 */
@RestController
@RequestMapping(value = "/api/v1", produces = JSON_API_VALUE)
public class ResourceNameIdentifierRepository extends ResourceNameIdentifierBaseRepository {

  public ResourceNameIdentifierRepository(
    ResourceNameIdentifierService resourceNameIdentifierService,
    GroupWithReadAuthorizationService authorizationService) {
    super(resourceNameIdentifierService, authorizationService,
      Map.of(ObjectStoreMetadataDto.TYPENAME, ObjectStoreMetadata.class));
  }

  @GetMapping(ResourceNameIdentifierResponseDto.TYPE)
  public ResponseEntity<?> findAll(HttpServletRequest req) {
    List<ResourceNameIdentifierResponseDto> dtos ;

    try {
      String query = URLDecoder.decode(req.getQueryString(), StandardCharsets.UTF_8);

      List<NameUUIDPair> identifiers = findAll(query);
      dtos = identifiers.stream().map(nuPair -> ResourceNameIdentifierResponseDto.builder()
        .id(nuPair.uuid())
        .name(nuPair.name())
        .build()).toList();

    } catch (IllegalArgumentException iaEx) {
      return ResponseEntity.badRequest().body(
        JsonApiErrors.create().withError(
          JsonApiError.create()
            .withTitle(HttpStatus.BAD_REQUEST.toString())
            .withStatus(String.valueOf(HttpStatus.BAD_REQUEST.value()))
            .withDetail(iaEx.getMessage())));
    }

    JsonApiModelBuilder builder = jsonApiModel().model(CollectionModel.of(dtos));

    return ResponseEntity.ok(builder.build());
  }

}
