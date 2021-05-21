package ca.gc.aafc.objectstore.api.rest;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.MinioTestConfiguration;
import ca.gc.aafc.objectstore.api.dto.ManagedAttributeDto;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.repository.ManagedAttributeResourceRepository;
import ca.gc.aafc.objectstore.api.repository.ObjectStoreResourceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.crnk.core.queryspec.QuerySpec;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.javers.core.Javers;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.UUID;

public class ManagedAttributeAuditingIT extends BaseIntegrationTest {

  @Inject
  private ObjectStoreResourceRepository metadataRepository;

  @Inject
  private ManagedAttributeResourceRepository managedRepo;

  @Inject
  private Javers javers;

  @Inject
  private ObjectMapper objectMapper;

  private ObjectUpload objectUpload;
  private ManagedAttributeDto managedAttribute;

  @BeforeEach
  void setUp() {
    objectUpload = MinioTestConfiguration.buildTestObjectUpload();
    objectUploadService.create(objectUpload);

    ManagedAttributeDto managed = new ManagedAttributeDto();
    managed.setName("name");
    managed.setUuid(UUID.randomUUID());
    managed.setDescription(ImmutableMap.of("en", "en"));
    managed.setManagedAttributeType(ObjectStoreManagedAttribute.ManagedAttributeType.STRING);

    managedAttribute = managedRepo.create(managed);
  }

  @Test
  void update_WhenMetaDataRepo_ManagedAttributesNotAudited() {
    ObjectStoreMetadataDto meta = metadataRepository.create(newMeta());

    ObjectStoreMetadataDto updated = findMetaData(meta.getUuid());
    updated.setXmpRightsOwner("new owner");
    metadataRepository.save(updated);

    CdoSnapshot resultSnap = javers.getLatestSnapshot(meta.getUuid(), ObjectStoreMetadataDto.class)
      .orElse(null);
    Assertions.assertNotNull(resultSnap);
    MatcherAssert.assertThat(resultSnap.getChanged(), Matchers.contains("xmpRightsOwner"));
  }

  private ObjectStoreMetadataDto newMeta() {
    ObjectStoreMetadataDto dto = new ObjectStoreMetadataDto();
    dto.setFileIdentifier(objectUpload.getFileIdentifier());
    dto.setBucket("b");
    return dto;
  }

  private ObjectStoreMetadataDto findMetaData(UUID uuid) {
    QuerySpec querySpec = new QuerySpec(ObjectStoreMetadataDto.class);
    return metadataRepository.findOne(uuid, querySpec);
  }

}
