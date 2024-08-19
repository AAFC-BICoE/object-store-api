package ca.gc.aafc.objectstore.api.repository;

import io.crnk.core.exception.MethodNotAllowedException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import java.io.Serializable;
import java.util.Collection;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import ca.gc.aafc.objectstore.api.dto.ObjectExportDto;
import ca.gc.aafc.objectstore.api.service.ObjectExportService;

@Repository
public class ObjectExportRepository implements ResourceRepository<ObjectExportDto, Serializable> {

  private final ObjectExportService objectExportService;
  private final DinaAuthenticatedUser authenticatedUser;

  public ObjectExportRepository(ObjectExportService objectExportService,
                                DinaAuthenticatedUser authenticatedUser) {
    this.objectExportService = objectExportService;
    this.authenticatedUser = authenticatedUser;
  }

  @Override
  public <S extends ObjectExportDto> S create(S s) {

    UUID exportUUID =
      objectExportService.export(authenticatedUser.getUsername(), s.getFileIdentifiers(), s.getName());
    s.setUuid(exportUUID);
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
