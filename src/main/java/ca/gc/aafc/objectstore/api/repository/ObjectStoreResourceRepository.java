package ca.gc.aafc.objectstore.api.repository;

import ca.gc.aafc.dina.json.JsonDocumentInspector;
import ca.gc.aafc.dina.mapper.DinaMapper;
import ca.gc.aafc.dina.repository.DinaRepository;
import ca.gc.aafc.dina.repository.external.ExternalResourceProvider;
import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import ca.gc.aafc.dina.security.TextHtmlSanitizer;
import ca.gc.aafc.dina.service.AuditService;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.security.MetadataAuthorizationService;
import ca.gc.aafc.objectstore.api.service.ObjectStoreMetaDataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.queryspec.QuerySpec;
import java.util.Map;
import java.util.Set;
import lombok.NonNull;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.function.Predicate;

// CHECKSTYLE:OFF NoFinalizer
// CHECKSTYLE:OFF SuperFinalize
@Repository
@Transactional
public class ObjectStoreResourceRepository
  extends DinaRepository<ObjectStoreMetadataDto, ObjectStoreMetadata> {

  private final ObjectMapper objMapper;

  private Optional<DinaAuthenticatedUser> authenticatedUser;

  public ObjectStoreResourceRepository(
    @NonNull ObjectStoreMetaDataService dinaService,
    @NonNull ExternalResourceProvider externalResourceProvider,
    Optional<DinaAuthenticatedUser> authenticatedUser,
    @NonNull AuditService auditService,
    MetadataAuthorizationService metadataAuthorizationService,
    @NonNull BuildProperties props,
    @NonNull ObjectMapper objMapper
  ) {
    super(
      dinaService,
      metadataAuthorizationService,
      Optional.of(auditService),
      new DinaMapper<>(ObjectStoreMetadataDto.class),
      ObjectStoreMetadataDto.class,
      ObjectStoreMetadata.class,
      null,
      externalResourceProvider,
      props, objMapper);
    this.authenticatedUser = authenticatedUser;
    this.objMapper = objMapper;

  }

  /**
   * @param resource to save
   * @return Returns the completed Dto, instead of the incomplete one normally returned.
   */
  @Override
  @SuppressWarnings("unchecked")
  public <S extends ObjectStoreMetadataDto> S save(S resource) {
    S dto = super.save(resource);
    return (S) this.findOne(dto.getUuid(), new QuerySpec(ObjectStoreMetadataDto.class));
  }

  @SuppressWarnings("unchecked")
  @Override
  public ObjectStoreMetadataDto create(ObjectStoreMetadataDto resource) {
    authenticatedUser.ifPresent(
      user -> resource.setCreatedBy(user.getUsername())
    );
    return super.create(resource);
  }

  @Override
  protected <S extends ObjectStoreMetadataDto> void checkSubmittedData(S resource) {
    Map<String, Object> convertedObj = objMapper.convertValue(resource, IT_OM_TYPE_REF);
    // if it is a known resource class limit validation to attributes and exclude relationships
    Set<String> attributesForClass = registry.getAttributesPerClass().get(resource.getClass());
    if (attributesForClass != null) {
      convertedObj.keySet().removeIf(k -> !attributesForClass.contains(k));
    }

    //remove managedAttributes to allow OCR data
    convertedObj.remove("managedAttributes");

    if (!JsonDocumentInspector.testPredicateOnValues(convertedObj, supplyCheckSubmittedDataPredicate())) {
      throw new IllegalArgumentException("Unaccepted value detected in attributes");
    }
  }

  @Override
  protected Predicate<String> supplyCheckSubmittedDataPredicate() {
    return txt -> TextHtmlSanitizer.isSafeText(txt) || TextHtmlSanitizer.isAcceptableText(txt);
  }

  /**
   * Protection against CT_CONSTRUCTOR_THROW
   */
  @Override
  protected final void finalize() {
    // no-op
  }

}
