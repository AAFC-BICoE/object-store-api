package ca.gc.aafc.objectstore.api.repository;

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
import lombok.NonNull;
import org.jsoup.safety.Safelist;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.function.Predicate;

@Repository
@Transactional
public class ObjectStoreResourceRepository
  extends DinaRepository<ObjectStoreMetadataDto, ObjectStoreMetadata> {

  private static final Safelist NONE_SAFELIST = Safelist.none();

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
  protected Predicate<String> supplyPredicate() {
    return (txt) -> isSafeText(txt) || TextHtmlSanitizer.isAcceptableText(txt);
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
