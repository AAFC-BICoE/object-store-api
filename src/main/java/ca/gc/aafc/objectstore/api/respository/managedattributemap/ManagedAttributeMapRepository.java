package ca.gc.aafc.objectstore.api.respository.managedattributemap;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.mapper.DinaMapper;
import ca.gc.aafc.dina.mapper.DinaMappingLayer;
import ca.gc.aafc.dina.service.AuditService;
import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.objectstore.api.dto.ManagedAttributeMapDto;
import ca.gc.aafc.objectstore.api.dto.ManagedAttributeMapDto.ManagedAttributeMapValue;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import io.crnk.core.exception.MethodNotAllowedException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

/**
 * Resource repository for adding Managed Attribute values in a more client-friendly way than
 * manually adding MetadataManagedAttributes.
 *
 * ManagedAttributeMap is a derived object to conveniently/compactly get/set a Metadata's ManagedAttribute values.
 *
 * Provides a POST endpoint for adding new ManagedAttribute values for a Metadata.
 *
 * Example POST request body to /api/managed-attribute-map:
 * {
 *   "data": {
 *     "type": "managed-attribute-map",
 *     "attributes": {
 *       "values": {
 *         // The UUID refers to the ManagedAttribute's UUID
 *         "51451dcd-a2c5-45fb-8dba-d4c26b60169e": { "value": "example value" },
 *         "d7c0d0a7-aef2-462d-9dc0-deb85f4ce85b": { "value": "example value 2" }
 *       }
 *     }
 *     "relationships": {
 *       "metadata": {
 *         "data": {
 *           { "type": "metadata", "id": "de29c062-6686-412a-b71e-677c83d0c3aa" }
 *         }
 *       }
 *     }
 *   }
 * }
 */
@Repository
@Transactional
public class ManagedAttributeMapRepository extends ResourceRepositoryBase<ManagedAttributeMapDto, UUID> {

  private final BaseDAO dao;
  private final AuditService auditService;
  private final DinaMappingLayer<ObjectStoreMetadataDto, ObjectStoreMetadata> mappingLayer;

  @Inject
  public ManagedAttributeMapRepository(final BaseDAO baseDao, AuditService auditService) {
    super(ManagedAttributeMapDto.class);
    this.dao = baseDao;
    this.auditService = auditService;
    this.mappingLayer = new DinaMappingLayer<>(
      ObjectStoreMetadataDto.class,
      new DinaService<>(baseDao),
      new DinaMapper<>(ObjectStoreMetadataDto.class));
  }

  @Override
  public ResourceList<ManagedAttributeMapDto> findAll(final QuerySpec querySpec) {
    throw new MethodNotAllowedException("method not allowed");
  }

  @Override
  public <S extends ManagedAttributeMapDto> S create(final S resource) {
    // Get the target metadata:
    final UUID metadataUuid = Optional.ofNullable(resource.getMetadata())
      .map(ObjectStoreMetadataDto::getUuid)
      .orElseThrow(() -> new ValidationException(
        "Metadata relationship required to add managed attributes map."));
    final ObjectStoreMetadata metadata = dao.findOneByNaturalId(
      metadataUuid,
      ObjectStoreMetadata.class);

    final List<MetadataManagedAttribute> managedAttributeValues =
      metadata.getManagedAttribute() == null ? new ArrayList<>() : metadata.getManagedAttribute();
    metadata.setManagedAttribute(managedAttributeValues);

    // Loop through the changed attribute values:
    for (final Entry<String, ManagedAttributeMapValue> entry : resource.getValues().entrySet()) {
      final UUID changedAttributeUuid = UUID.fromString(entry.getKey());
      final ManagedAttribute changedAttribute = dao.findOneByNaturalId(
        changedAttributeUuid,
        ManagedAttribute.class);
      final String newValue = entry.getValue().getValue();

      final Optional<MetadataManagedAttribute> existingAttributeValue = managedAttributeValues.stream()
        .filter(existingAttr -> existingAttr.getManagedAttribute() == changedAttribute)
        .findFirst();

      // If this attribute is already set then update the value :
      existingAttributeValue.ifPresent(val -> {
        if (StringUtils.isBlank(newValue)) {
          dao.delete(val);
          managedAttributeValues.remove(val);
        } else {
          val.setAssignedValue(newValue);
        }
      });

      // If there is no existing value then create a new one:
      if (existingAttributeValue.isEmpty() && !StringUtils.isBlank(newValue)) {
        final MetadataManagedAttribute newAttributeValue = MetadataManagedAttribute.builder()
          .managedAttribute(changedAttribute)
          .objectStoreMetadata(metadata)
          .createdBy(changedAttribute.getCreatedBy())
          .assignedValue(newValue)
          .uuid(UUID.randomUUID())
          .build();
        dao.create(newAttributeValue);
        managedAttributeValues.add(newAttributeValue);
      }
    }

    // Crnk requires a created resource to have an ID. Create one here if the client did not provide one.
    resource.setId(Optional.ofNullable(resource.getId()).orElse("N/A"));

    // flush all jpa changes
    dao.update(metadata);
    // map to dto and audit
    mapMetadata(metadata).ifPresent(dto -> {
      dto.setManagedAttributeMap(
        MetadataToManagedAttributeMapRepository.getAttributeMapFromMetadata(metadata));
      auditService.audit(dto);
    });
    return resource;
  }

  private Optional<ObjectStoreMetadataDto> mapMetadata(ObjectStoreMetadata metadata) {
    return mappingLayer.mapEntitiesToDto(
      new QuerySpec(ObjectStoreMetadataDto.class), Collections.singletonList(metadata)
    ).stream().findFirst();
  }

}
