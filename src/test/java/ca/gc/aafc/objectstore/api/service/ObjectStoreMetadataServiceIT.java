package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute.ManagedAttributeType;
import ca.gc.aafc.objectstore.api.exif.ExifParser;
import ca.gc.aafc.objectstore.api.testsupport.factories.MetadataManagedAttributeFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectStoreMetadataServiceIT extends BaseIntegrationTest {

  @Inject
  private ObjectStoreMetaDataService objectStoreMetaDataService;

  @Test
  public void objectStoreMetaDataService_OnUpdate_InvalidMetadataManagedAttributeValue_throwException() {

    ObjectStoreMetadata testMetadata = ObjectStoreMetadataFactory.newObjectStoreMetadata()
        .managedAttribute(Collections.singletonList( MetadataManagedAttributeFactory.newMetadataManagedAttribute()        
        .build()))
        .build();

    ObjectStoreMetadata testMetadataCreated =  objectStoreMetaDataService.create(testMetadata);

    Assertions.assertNotNull(testMetadataCreated);

    testMetadataCreated.getManagedAttribute().get(0).getManagedAttribute().setManagedAttributeType(ManagedAttributeType.INTEGER);
    testMetadataCreated.getManagedAttribute().get(0).getManagedAttribute().setAcceptedValues(null);    

    Assertions.assertThrows(IllegalArgumentException.class, () -> testMetadataCreated.getManagedAttribute().get(0).setAssignedValue("test"));

  }

}
