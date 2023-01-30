package ca.gc.aafc.objectstore.api.repository;

import ca.gc.aafc.dina.json.JsonDocumentInspector;
import ca.gc.aafc.dina.mapper.DinaMapper;
import ca.gc.aafc.dina.mapper.DinaMappingRegistry;
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
import lombok.NonNull;
import org.jsoup.safety.Safelist;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Repository
@Transactional
public class ObjectStoreResourceRepository
  extends DinaRepository<ObjectStoreMetadataDto, ObjectStoreMetadata> {

  private static final Safelist NONE_SAFELIST = Safelist.none();

  private Optional<DinaAuthenticatedUser> authenticatedUser;
  private final ObjectMapper objMapper;

  //The base class should expose it
  private final DinaMappingRegistry registry;

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

    // should be exposed by base class to avoid an unnecessary second instance
    this.registry = new DinaMappingRegistry(ObjectStoreMetadataDto.class);
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

  /**
   * We override the checkMethod to use a less aggressive check since Determination can have simple text html.
   * @param resource
   * @param <S>
   */
  @Override
  protected <S extends ObjectStoreMetadataDto> void checkSubmittedData(S resource) {
    Objects.requireNonNull(this.objMapper);
    Map<String, Object> convertedObj = (Map)this.objMapper.convertValue(resource, IT_OM_TYPE_REF);
    Set<String> attributesForClass = (Set)this.registry.getAttributesPerClass().get(resource.getClass());
    if (attributesForClass != null) {
      convertedObj.keySet().removeIf(k -> !attributesForClass.contains(k));
    }

    if (!JsonDocumentInspector.testPredicateOnValues(convertedObj, ObjectStoreResourceRepository::isSafeText)) {
      throw new IllegalArgumentException("unsafe value detected in attributes");
    }
  }

  /**
   * Custom function to allow unescapedEntities since the ocr field often has some.
   * @param txt
   * @return
   */
  private static boolean isSafeText(String txt) {
    return TextHtmlSanitizer.isSafeText(txt, NONE_SAFELIST, true);
  }

}
