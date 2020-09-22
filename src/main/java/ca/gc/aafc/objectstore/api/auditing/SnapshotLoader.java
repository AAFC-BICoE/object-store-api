package ca.gc.aafc.objectstore.api.auditing;

import ca.gc.aafc.dina.mapper.JpaDtoMapper;
import ca.gc.aafc.objectstore.api.dto.ManagedAttributeMapDto;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.respository.managedattributemap.MetadataToManagedAttributeMapRepository;
import com.google.common.collect.ImmutableMap;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.queryspec.QuerySpec;
import lombok.RequiredArgsConstructor;

import javax.inject.Named;
import java.util.Map;
import java.util.function.Function;

/**
 * Provides entity-specific loading of audit snapshots.
 * Auditable DTOs must have snapshot loaders defined in this class.
 */
@Named
@RequiredArgsConstructor
public class SnapshotLoader {

  private final JpaDtoMapper jpaDtoMapper;
  private final MetadataToManagedAttributeMapRepository managedAttributeMapRepo;
  private final CrnkBoot boot;

  /** Map from entity class to snapshot loader function. */
  private final Map<Class<?>, Function<Object, Object>> loaders = ImmutableMap.<Class<?>, Function<Object, Object>>builder()
      .put(ObjectStoreMetadata.class, this::loadMetadataSnapshot)
      .build();

  public boolean supports(Class<?> entityClass) {
    return this.loaders.containsKey(entityClass);
  }

  public Object loadSnapshot(Object entity) {
    Function<Object, Object> loader = this.loaders.get(entity.getClass());
    return loader.apply(entity);
  }

  private Object loadMetadataSnapshot(Object entity) {
    QuerySpec querySpec = new QuerySpec(ObjectStoreMetadataDto.class);

    ObjectStoreMetadataDto metadata = (ObjectStoreMetadataDto) jpaDtoMapper
        .toDto(entity, querySpec,  boot.getResourceRegistry());

    // Fetch the managed attribute map from the repo, because it isn't included through the QuerySpec.
    ManagedAttributeMapDto attributeMap = managedAttributeMapRepo
        .getAttributeMapFromMetadata((ObjectStoreMetadata) entity);
    metadata.setManagedAttributeMap(attributeMap);

    return metadata;
  }

}
