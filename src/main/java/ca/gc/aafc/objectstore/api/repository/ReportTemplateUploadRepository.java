package ca.gc.aafc.objectstore.api.repository;

import org.springframework.boot.info.BuildProperties;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder;

import ca.gc.aafc.dina.dto.JsonApiDto;
import ca.gc.aafc.dina.exception.ResourceNotFoundException;
import ca.gc.aafc.dina.jsonapi.JsonApiDocument;
import ca.gc.aafc.dina.repository.JsonApiModelAssistant;
import ca.gc.aafc.dina.security.auth.ObjectOwnerAuthorizationService;
import ca.gc.aafc.objectstore.api.config.MediaTypeConfiguration;
import ca.gc.aafc.objectstore.api.dto.ObjectUploadDto;
import ca.gc.aafc.objectstore.api.dto.ReportTemplateUploadDto;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.service.ReportTemplateUploadService;

import static com.toedter.spring.hateoas.jsonapi.MediaTypes.JSON_API_VALUE;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1", produces = JSON_API_VALUE)
public class ReportTemplateUploadRepository {

  private final ReportTemplateUploadService reportTemplateUploadService;
  private final ObjectOwnerAuthorizationService authorizationService;

  private final ObjectMapper objMapper;
  private final JsonApiModelAssistant<ReportTemplateUploadDto> jsonApiModelAssistant;

  public ReportTemplateUploadRepository(ReportTemplateUploadService reportTemplateUploadService,
                                        ObjectOwnerAuthorizationService authorizationService,
                                        ObjectMapper objMapper,
                                        BuildProperties buildProperties) {
    this.reportTemplateUploadService = reportTemplateUploadService;
    this.authorizationService = authorizationService;

    this.objMapper = objMapper;
    this.jsonApiModelAssistant = new JsonApiModelAssistant<>(buildProperties.getVersion());
  }

  @Transactional(readOnly = true)
  @PostMapping(ReportTemplateUploadDto.TYPENAME)
  public ResponseEntity<RepresentationModel<?>> onCreate(@RequestBody JsonApiDocument postedDocument)
      throws ResourceNotFoundException {

    ReportTemplateUploadDto dto = this.objMapper.convertValue(postedDocument.getAttributes(), ReportTemplateUploadDto.class);
    try {
      ObjectUpload objectUpload = reportTemplateUploadService.findOneObjectUpload(dto.getFileIdentifier());

      if (objectUpload == null) {
        throw ResourceNotFoundException.create(ObjectUploadDto.TYPENAME, dto.getFileIdentifier());
      }

      // we are using authorizeCreate but, it's only to check the object ownership
      authorizationService.authorizeCreate(objectUpload);

      if (!MediaTypeConfiguration.FREEMARKER_TEMPLATE_MIME_TYPE.toString().equals(objectUpload.getEvaluatedMediaType())) {
        throw new UnsupportedMediaTypeStatusException("Only " + MediaTypeConfiguration.FREEMARKER_TEMPLATE_MIME_TYPE + " is accepted.");
      }

      UUID reportTemplateUUID = reportTemplateUploadService.handleTemplateUpload(dto.getFileIdentifier()).uuid();
      dto.setUuid(reportTemplateUUID);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }

    JsonApiDto<ReportTemplateUploadDto> jsonApiDto = JsonApiDto.<ReportTemplateUploadDto>builder()
      .dto(dto).build();
    JsonApiModelBuilder builder = this.jsonApiModelAssistant.createJsonApiModelBuilder(jsonApiDto);
    RepresentationModel<?> model = builder.build();
    // no specific url to provide
    URI uri = URI.create(ReportTemplateUploadDto.TYPENAME);
    return ResponseEntity.created(uri).body(model);
  }

}
