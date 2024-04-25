package ca.gc.aafc.objectstore.api.repository;

import io.crnk.core.exception.MethodNotAllowedException;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.stereotype.Component;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import ca.gc.aafc.dina.security.auth.ObjectOwnerAuthorizationService;
import ca.gc.aafc.objectstore.api.config.MediaTypeConfiguration;
import ca.gc.aafc.objectstore.api.dto.ReportTemplateUploadDto;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.service.ReportTemplateUploadService;

@Component
public class ReportTemplateUploadRepository
  implements ResourceRepository<ReportTemplateUploadDto, Serializable> {

  private final ReportTemplateUploadService reportTemplateUploadService;
  private final ObjectOwnerAuthorizationService authorizationService;

  public ReportTemplateUploadRepository(ReportTemplateUploadService reportTemplateUploadService,
                                        ObjectOwnerAuthorizationService authorizationService) {
    this.reportTemplateUploadService = reportTemplateUploadService;
    this.authorizationService = authorizationService;
  }

  @Override
  @Transactional(readOnly = true)
  public <S extends ReportTemplateUploadDto> S create(S s) {
    try {

      ObjectUpload objectUpload = reportTemplateUploadService.findOneObjectUpload(s.getFileIdentifier());

      if(objectUpload == null) {
        throw new ResourceNotFoundException("ObjectUpload with ID " + s.getFileIdentifier() + " Not Found.");
      }

      // we are using authorizeCreate but, it's only to check the object ownership
      authorizationService.authorizeCreate(objectUpload);

      if(!MediaTypeConfiguration.FREEMARKER_TEMPLATE_MIME_TYPE.toString().equals(objectUpload.getEvaluatedMediaType())) {
        throw new UnsupportedMediaTypeStatusException("Only " + MediaTypeConfiguration.FREEMARKER_TEMPLATE_MIME_TYPE + " is accepted.");
      }

      UUID reportTemplateUUID = reportTemplateUploadService.handleTemplateUpload(s.getFileIdentifier()).uuid();
      s.setUuid(reportTemplateUUID);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    return s;
  }

  @Override
  public Class<ReportTemplateUploadDto> getResourceClass() {
    return ReportTemplateUploadDto.class;
  }

  @Override
  public ReportTemplateUploadDto findOne(Serializable serializable, QuerySpec querySpec) {
    return null;
  }

  @Override
  public ResourceList<ReportTemplateUploadDto> findAll(QuerySpec querySpec) {
    throw new MethodNotAllowedException("GET");
  }

  @Override
  public ResourceList<ReportTemplateUploadDto> findAll(Collection<Serializable> collection,
                                                       QuerySpec querySpec) {
    throw new MethodNotAllowedException("GET");
  }

  @Override
  public <S extends ReportTemplateUploadDto> S save(S s) {
    throw new MethodNotAllowedException("PUT/PATCH");
  }

  @Override
  public void delete(Serializable serializable) {
    throw new MethodNotAllowedException("DELETE");
  }
}
