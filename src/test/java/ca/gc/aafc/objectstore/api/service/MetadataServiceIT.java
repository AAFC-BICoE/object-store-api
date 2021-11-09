package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MetadataServiceIT extends BaseIntegrationTest {

  @Test
  public void createMetadata_OnCr2_DcTypeIsCorrectlySet() {
    ObjectUpload objectUploadTest = ObjectUploadFactory.newObjectUpload()
        .evaluatedFileExtension(".cr2")
        .detectedMediaType("image/x-canon-cr2")
        .evaluatedMediaType("image/x-canon-cr2")
        .build();
    objectUploadService.create(objectUploadTest);

    ObjectStoreMetadata metadata = new ObjectStoreMetadata();
    metadata.setBucket(objectUploadTest.getBucket());
    metadata.setFileIdentifier(objectUploadTest.getFileIdentifier());
    metadata.setXmpRightsUsageTerms(ObjectUploadFactory.TEST_USAGE_TERMS);
    metadata.setCreatedBy(RandomStringUtils.random(4));
    metadata.setManagedAttributeValues(Map.of());

    // default value setter is expected to set dcType based on the evaluatedMediaType
    // evaluatedMediaType -> dcFormat (image/x-canon-cr2) -> dcType (IMAGE)
    UUID metadataUuid = objectStoreMetaDataService.create(metadata).getUuid();

    ObjectStoreMetadata result = objectStoreMetaDataService.findOne(metadataUuid);
    assertEquals(metadataUuid, result.getUuid());
    assertEquals(objectUploadTest.getEvaluatedMediaType(), result.getDcFormat());
    assertEquals(objectUploadTest.getFileIdentifier(), result.getFileIdentifier());
    assertEquals(DcType.IMAGE, result.getDcType());
    assertEquals(ObjectUploadFactory.TEST_USAGE_TERMS, result.getXmpRightsUsageTerms());
  }
}
