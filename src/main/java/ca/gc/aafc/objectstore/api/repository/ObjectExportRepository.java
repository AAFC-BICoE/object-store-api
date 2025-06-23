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
import ca.gc.aafc.dina.jsonapi.JsonApiDocument;
import ca.gc.aafc.dina.repository.JsonApiModelAssistant;
import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import ca.gc.aafc.objectstore.api.dto.ObjectExportDto;
import ca.gc.aafc.objectstore.api.mapper.ObjectExportMapper;
import ca.gc.aafc.objectstore.api.service.ObjectExportService;

import static com.toedter.spring.hateoas.jsonapi.MediaTypes.JSON_API_VALUE;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping(value = "${dina.apiPrefix:}", produces = JSON_API_VALUE)
public class ObjectExportRepository {

  private final ObjectExportService objectExportService;
  private final ObjectExportMapper mapper;

  private final JsonApiModelAssistant<ObjectExportDto> jsonApiModelAssistant;
  private final ObjectMapper objMapper;

  private final DinaAuthenticatedUser authenticatedUser;

  public ObjectExportRepository(ObjectExportService objectExportService,
                                DinaAuthenticatedUser authenticatedUser,
                                BuildProperties buildProperties,
                                ObjectMapper objMapper) {
    this.objectExportService = objectExportService;
    this.objMapper = objMapper;
    this.authenticatedUser = authenticatedUser;
    this.mapper = ObjectExportMapper.INSTANCE;
    this.jsonApiModelAssistant = new JsonApiModelAssistant<>(buildProperties.getVersion());
  }

  @PostMapping(ObjectExportDto.TYPENAME)
  public ResponseEntity<RepresentationModel<?>> onCreate(@RequestBody JsonApiDocument postedDocument) {

    ObjectExportDto dto = this.objMapper.convertValue(postedDocument.getAttributes(), ObjectExportDto.class);
    dto.setUsername(authenticatedUser.getUsername());
    ObjectExportService.ExportArgs exportArgs = mapper.toEntity(dto);


    UUID exportUUID = objectExportService.export(exportArgs);

    dto.setUuid(exportUUID);
    JsonApiDto<ObjectExportDto> jsonApiDto = JsonApiDto.<ObjectExportDto>builder()
      .dto(dto).build();
    JsonApiModelBuilder builder = this.jsonApiModelAssistant.createJsonApiModelBuilder(jsonApiDto);
    RepresentationModel<?> model = builder.build();
    // this will be available in data-export using that same uuid
    URI uri = URI.create("data-export/" + exportUUID);
    return ResponseEntity.created(uri).body(model);
  }
}
