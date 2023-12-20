package ca.gc.aafc.objectstore.api.repository;

import io.crnk.core.exception.MethodNotAllowedException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;

import org.springframework.stereotype.Repository;

import ca.gc.aafc.objectstore.api.dto.ObjectExportDto;
import ca.gc.aafc.objectstore.api.service.ObjectExportService;

@Repository
public class ObjectExportRepository implements ResourceRepository<ObjectExportDto, Serializable> {

  private final ObjectExportService objectExportService;

  public ObjectExportRepository(ObjectExportService objectExportService) {
    this.objectExportService = objectExportService;
  }

  @Override
  public <S extends ObjectExportDto> S create(S s) {
    try {
      ObjectExportService.ExportResult exportResult = objectExportService.export(s.getFileIdentifiers());
      s.setUuid(exportResult.uuid());
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    return s;
  }

  @Override
  public Class<ObjectExportDto> getResourceClass() {
    return ObjectExportDto.class;
  }

  @Override
  public ObjectExportDto findOne(Serializable serializable, QuerySpec querySpec) {
    return null;
  }

  @Override
  public ResourceList<ObjectExportDto> findAll(QuerySpec querySpec) {
    throw new MethodNotAllowedException("GET");
  }

  @Override
  public ResourceList<ObjectExportDto> findAll(Collection<Serializable> collection, QuerySpec querySpec) {
    throw new MethodNotAllowedException("GET");
  }

  @Override
  public <S extends ObjectExportDto> S save(S s) {
    throw new MethodNotAllowedException("PUT/PATCH");
  }

//  protected <S extends ReportRequestDto> void checkSubmittedData(S resource) {
//    Objects.requireNonNull(objMapper);
//    Map<String, Object> convertedObj = objMapper.convertValue(resource, MAP_TYPEREF);
//    if (!JsonDocumentInspector.testPredicateOnValues(convertedObj, TextHtmlSanitizer::isSafeText)) {
//      throw new IllegalArgumentException("Unaccepted value detected in attributes");
//    }
//  }

  @Override
  public void delete(Serializable serializable) {
    throw new MethodNotAllowedException("DELETE");
  }
}
