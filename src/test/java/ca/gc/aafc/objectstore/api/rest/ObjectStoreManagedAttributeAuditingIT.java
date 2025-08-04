package ca.gc.aafc.objectstore.api.rest;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.javers.core.Javers;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.gc.aafc.dina.exception.ResourceGoneException;
import ca.gc.aafc.dina.exception.ResourceNotFoundException;
import ca.gc.aafc.dina.jsonapi.JsonApiDocument;
import ca.gc.aafc.dina.util.UUIDHelper;
import ca.gc.aafc.dina.vocabulary.TypedVocabularyElement;
import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreManagedAttributeDto;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.repository.ObjectStoreManagedAttributeResourceRepository;
import ca.gc.aafc.objectstore.api.repository.ObjectStoreMetadataRepositoryV2;
import ca.gc.aafc.objectstore.api.testsupport.factories.MultilingualDescriptionFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;

import static ca.gc.aafc.objectstore.api.repository.ObjectStoreModuleBaseRepositoryIT.dtoToJsonApiDocument;

import javax.inject.Inject;

public class ObjectStoreManagedAttributeAuditingIT extends BaseIntegrationTest {

  @Inject
  private ObjectStoreMetadataRepositoryV2 metadataRepository;

  @Inject
  private ObjectStoreManagedAttributeResourceRepository managedRepo;

  @Inject
  private Javers javers;

  private ObjectUpload objectUpload;

  @BeforeEach
  void setUp() {
    objectUpload = ObjectUploadFactory.buildTestObjectUpload();
    objectUploadService.create(objectUpload);

    ObjectStoreManagedAttributeDto managed = new ObjectStoreManagedAttributeDto();
    managed.setName("name");
    managed.setUuid(UUIDHelper.generateUUIDv7());
    managed.setMultilingualDescription(MultilingualDescriptionFactory.newMultilingualDescription().build());
    managed.setVocabularyElementType(TypedVocabularyElement.VocabularyElementType.STRING);
  }

  @Test
  void update_WhenMetaDataRepo_ManagedAttributesNotAudited()
    throws ResourceGoneException, ResourceNotFoundException {

    JsonApiDocument docToCreate = dtoToJsonApiDocument(newMeta());
    ObjectStoreMetadataDto meta = metadataRepository.create(docToCreate, null).getDto();

    ObjectStoreMetadataDto updated = metadataRepository.getOne(meta.getUuid(), "").getDto();
    updated.setXmpRightsOwner("new owner");
    JsonApiDocument docToUpdate = dtoToJsonApiDocument(updated);
    metadataRepository.update(docToUpdate);

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
}
