package ca.gc.aafc.objectstore.api.rest;

import ca.gc.aafc.dina.vocabulary.TypedVocabularyElement;
import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreManagedAttributeDto;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.repository.ObjectStoreManagedAttributeResourceRepository;
import ca.gc.aafc.objectstore.api.repository.ObjectStoreResourceRepository;
import ca.gc.aafc.objectstore.api.testsupport.factories.MultilingualDescriptionFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

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

public class ObjectStoreManagedAttributeAuditingIT extends BaseIntegrationTest {

  @Inject
  private ObjectStoreResourceRepository metadataRepository;

  @Inject
  private ObjectStoreManagedAttributeResourceRepository managedRepo;

  @Inject
  private Javers javers;

  @Inject
  private ObjectMapper objectMapper;

  private ObjectUpload objectUpload;

  @BeforeEach
  void setUp() {
    objectUpload = ObjectUploadFactory.buildTestObjectUpload();
    objectUploadService.create(objectUpload);

    ObjectStoreManagedAttributeDto managed = new ObjectStoreManagedAttributeDto();
    managed.setName("name");
    managed.setUuid(UUID.randomUUID());
    managed.setMultilingualDescription(MultilingualDescriptionFactory.newMultilingualDescription().build());
    managed.setManagedAttributeType(TypedVocabularyElement.VocabularyElementType.STRING);
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
