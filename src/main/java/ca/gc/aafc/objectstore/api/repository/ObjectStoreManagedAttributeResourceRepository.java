package ca.gc.aafc.objectstore.api.repository;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Repository;

import ca.gc.aafc.dina.mapper.DinaMapper;
import ca.gc.aafc.dina.repository.DinaRepository;
import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreManagedAttributeDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreManagedAttribute;
import ca.gc.aafc.objectstore.api.service.ObjectStoreManagedAttributeAuthorizationService;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import lombok.NonNull;

@Repository
public class ObjectStoreManagedAttributeResourceRepository
  extends DinaRepository<ObjectStoreManagedAttributeDto, ObjectStoreManagedAttribute> {

  private final Optional<DinaAuthenticatedUser> authenticatedUser;

  public ObjectStoreManagedAttributeResourceRepository(
    @NonNull DinaService<ObjectStoreManagedAttribute> dinaService,
    @NonNull ObjectStoreManagedAttributeAuthorizationService authorizationService,
    Optional<DinaAuthenticatedUser> authenticatedUser,
    @NonNull BuildProperties props
  ) {
    super(
      dinaService,
      authorizationService,
      Optional.empty(),
      new DinaMapper<>(ObjectStoreManagedAttributeDto.class),
      ObjectStoreManagedAttributeDto.class,
      ObjectStoreManagedAttribute.class, null, null,
      props);
    this.authenticatedUser = authenticatedUser;
  }

  @Override
  public <S extends ObjectStoreManagedAttributeDto> S create(S resource) {
    authenticatedUser.ifPresent(user -> resource.setCreatedBy(user.getUsername()));
    return super.create(resource);
  }

  @Override
  public ObjectStoreManagedAttributeDto findOne(Serializable id, QuerySpec querySpec) {
    boolean idIsUuid = true;
    try {
      UUID.fromString(id.toString());
    } catch (IllegalArgumentException exception) {
      idIsUuid = false;
    }
    
    // Try finding by UUID:
    if (idIsUuid) {
      return super.findOne(id, querySpec);
    }

    // Otherwise try a lookup by the managed attribute key.
    // e.g. GET /api/v1/managed-attribute/test-managed-attribute
    String attributeKey = id.toString();

    QuerySpec keyQuerySpec = new QuerySpec(ObjectStoreManagedAttributeDto.class);
    keyQuerySpec.addFilter(
      new FilterSpec(List.of("rsql"), FilterOperator.EQ, "key==" + attributeKey));
    
    var results = super.findAll(keyQuerySpec);
    if (results.size() == 1) {
      return results.get(0);
    } else {
      throw new ResourceNotFoundException("Managed Attribute not found: " + id);
    }
  }

}
