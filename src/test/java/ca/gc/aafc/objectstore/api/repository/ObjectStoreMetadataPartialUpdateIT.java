package ca.gc.aafc.objectstore.api.repository;

import ca.gc.aafc.dina.testsupport.BaseRestAssuredTest;
import ca.gc.aafc.dina.testsupport.DatabaseSupportService;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPIRelationship;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.respository.ObjectStoreResourceRepository;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@SpringBootTest(
  properties = {"keycloak.enabled: false"},
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class ObjectStoreMetadataPartialUpdateIT extends BaseRestAssuredTest {

  @Inject
  private ObjectStoreResourceRepository repoUnderTest;
  @Inject
  private DatabaseSupportService service;

  public ObjectUpload objectUpload1;
  public ObjectUpload objectUpload2;

  protected ObjectStoreMetadataPartialUpdateIT() {
    super("/api/v1");
  }

  @BeforeEach
  void setUp() {
    objectUpload1 = ObjectUploadFactory.newObjectUpload().evaluatedFileExtension(".txt").build();
    objectUpload2 = ObjectUploadFactory.newObjectUpload().evaluatedFileExtension(".txt").build();
    service.runInNewTransaction(em -> {
      em.persist(objectUpload1);
      em.persist(objectUpload2);
    });
  }

  @AfterEach
  void tearDown() {
    //Delete all metadata
    repoUnderTest.findAll(metaDataQuerySpec())
      .forEach(objectStoreMetadataDto -> repoUnderTest.delete(objectStoreMetadataDto.getUuid()));
    //Delete all ObjectUpload
    service.runInNewTransaction(em -> {
      CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
      CriteriaDelete<ObjectUpload> query = criteriaBuilder.createCriteriaDelete(ObjectUpload.class);
      Root<ObjectUpload> root = query.from(ObjectUpload.class);
      query.where(criteriaBuilder.isNotNull(root.get("id")));
      em.createQuery(query).executeUpdate();
    });
  }

  @Test
  void PartialUpdate() {
    ObjectStoreMetadataDto derivedFrom = newMetaData(objectUpload2);
    super.sendPost(ObjectStoreMetadataDto.TYPENAME, metaDataToMap(derivedFrom));

    ObjectStoreMetadataDto metadata = newMetaData(objectUpload1);
    metadata.setAcDerivedFrom(findMetaByFileID(objectUpload2.getFileIdentifier()));

    super.sendPost(ObjectStoreMetadataDto.TYPENAME, metaDataToMap(metadata));

    //assert the persisted meta data has the correct state
    assertMetaData(metadata, findMetaByFileID(objectUpload1.getFileIdentifier()));

    //Send empty patch
    super.sendPatch(
      ObjectStoreMetadataDto.TYPENAME,
      findMetaByFileID(objectUpload1.getFileIdentifier()).getUuid().toString(),
      ImmutableMap.of(
        "data",
        ImmutableMap.of(
          "type", "metadata",
          "attributes", Collections.emptyMap()
        )));

    //assert nothing has changed
    assertMetaData(metadata, findMetaByFileID(objectUpload1.getFileIdentifier()));
  }

  private void assertMetaData(ObjectStoreMetadataDto expected, ObjectStoreMetadataDto result) {
    Assertions.assertEquals(expected.getAcCaption(), result.getAcCaption());
    Assertions.assertEquals(expected.getBucket(), result.getBucket());
    Assertions.assertEquals(expected.getFileIdentifier(), result.getFileIdentifier());
    if (expected.getAcDerivedFrom() != null) {
      Assertions.assertNotNull(result.getAcDerivedFrom());
      assertMetaData(expected.getAcDerivedFrom(), result.getAcDerivedFrom());
    } else {
      Assertions.assertNull(result.getAcDerivedFrom());
    }
  }

  private ObjectStoreMetadataDto findMetaByFileID(UUID fileIdentifier) {
    QuerySpec querySpec = metaDataQuerySpec();
    querySpec.addFilter(PathSpec.of("fileIdentifier").filter(FilterOperator.EQ, fileIdentifier));
    return repoUnderTest.findAll(querySpec).stream().findFirst().orElse(null);
  }

  private static QuerySpec metaDataQuerySpec() {
    QuerySpec querySpec = new QuerySpec(ObjectStoreMetadataDto.class);
    querySpec.includeRelation(PathSpec.of("acDerivedFrom"));
    return querySpec;
  }

  private static Map<String, Object> metaDataToMap(ObjectStoreMetadataDto meta) {
    ObjectStoreMetadataDto temp = meta.getAcDerivedFrom();
    Map<String, Object> relationshipMap = null;
    if (temp != null) {
      relationshipMap = JsonAPITestHelper.toRelationshipMap(JsonAPIRelationship.of(
        "acDerivedFrom", "metadata", temp.getUuid().toString()));
      meta.setAcDerivedFrom(null); //cannot write this into the attribute map
    }
    Map<String, Object> attributeMap = JsonAPITestHelper.toAttributeMap(meta);
    meta.setAcDerivedFrom(temp);
    return JsonAPITestHelper.toJsonAPIMap(
      ObjectStoreMetadataDto.TYPENAME,
      attributeMap,
      relationshipMap,
      null);
  }

  private static ObjectStoreMetadataDto newMetaData(ObjectUpload upload) {
    ObjectStoreMetadataDto dto = new ObjectStoreMetadataDto();
    dto.setAcCaption(RandomStringUtils.randomAlphabetic(5));
    dto.setBucket(upload.getBucket());
    dto.setFileIdentifier(upload.getFileIdentifier());
    return dto;
  }

}