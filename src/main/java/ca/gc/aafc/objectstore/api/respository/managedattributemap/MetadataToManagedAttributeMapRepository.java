package ca.gc.aafc.objectstore.api.respository.managedattributemap;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.objectstore.api.dto.ManagedAttributeMapDto;
import ca.gc.aafc.objectstore.api.dto.ManagedAttributeMapDto.ManagedAttributeMapValue;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.OneRelationshipRepositoryBase;
import io.crnk.core.repository.RelationshipMatcher;

/**
 * Fetches the ManagedAttributeMap for a given Metadata.
 * 
 * ManagedAttributeMap is a derived object to conveniently/compactly get/set a Metadata's ManagedAttribute values.
 */
@Repository
@Transactional
public class MetadataToManagedAttributeMapRepository
    extends OneRelationshipRepositoryBase<ObjectStoreMetadataDto, UUID, ManagedAttributeMapDto, UUID> {

  private final BaseDAO dao;

  @Inject
  public MetadataToManagedAttributeMapRepository(BaseDAO dao) {
    this.dao = dao;
  }

  @Override
  public RelationshipMatcher getMatcher() {
    RelationshipMatcher matcher = new RelationshipMatcher();
    matcher.rule().source(ObjectStoreMetadataDto.class).target(ManagedAttributeMapDto.class).add();
    return matcher;
  }

  @Override
  public Map<UUID, ManagedAttributeMapDto> findOneRelations(Collection<UUID> sourceIds, String fieldName,
      QuerySpec querySpec) {
    Map<UUID, ManagedAttributeMapDto> findOneMap = new HashMap<>();

    for (UUID metadataUuid : sourceIds) {
      ObjectStoreMetadata metadata = dao.findOneById(metadataUuid, ObjectStoreMetadata.class);
      List<MetadataManagedAttribute> attrs = metadata.getManagedAttribute();

      // Build the attribute values map:
      Map<String, ManagedAttributeMapValue> attrValuesMap = new HashMap<>();
      for (MetadataManagedAttribute attr : attrs) {
        attrValuesMap.put(
          attr.getManagedAttribute().getUuid().toString(),
          ManagedAttributeMapValue.builder()
            .name(attr.getManagedAttribute().getName())
            .value(attr.getAssignedValue())
            .build()
        );
      }

      ManagedAttributeMapDto attrMap = ManagedAttributeMapDto.builder()
        .id("metadata/" + metadata.getUuid() + "/managedAttributeMap") // This is a generated/derived object, so it doesn't have its own ID.
        .values(attrValuesMap)
        .build();

      findOneMap.put(metadataUuid, attrMap);
    }

    return findOneMap;
  }

}
