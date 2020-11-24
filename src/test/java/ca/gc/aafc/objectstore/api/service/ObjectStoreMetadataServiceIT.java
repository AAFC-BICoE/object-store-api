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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectStoreMetadataServiceIT extends BaseIntegrationTest {

  @Inject
  private ObjectStoreMetaDataService objectStoreMetaDataService;

  private ObjectStoreMetadata testMetadata;
  @BeforeEach
  void setup(){
    testMetadata = ObjectStoreMetadataFactory.newObjectStoreMetadata()
    .managedAttribute(Collections.singletonList( MetadataManagedAttributeFactory.newMetadataManagedAttribute()        
    .build()))
    .build();
  }

  @Test
  public void objectStoreMetaDataService_OnCreate_InvalidMetadataManagedAttributeValue_throwException() {
    //invalid assigned value at creation time should throw exception
    Assertions.assertThrows(IllegalArgumentException.class, () -> objectStoreMetaDataService.create(testMetadata));        

    //fix the assigned value to a valid one
    testMetadata.getManagedAttribute().get(0).setAssignedValue("a");
    ObjectStoreMetadata testMetadataCreated = objectStoreMetaDataService.create(testMetadata);
    Assertions.assertNotNull(testMetadataCreated);
  }

  @Test
  public void objectStoreMetaDataService_OnUpdate_InvalidMetadataManagedAttributeValue_throwException() {
    //First persit an objectstore metadata via service
    testMetadata.getManagedAttribute().get(0).setAssignedValue("a");
    ObjectStoreMetadata testMetadataCreated = objectStoreMetaDataService.create(testMetadata);
    Assertions.assertNotNull(testMetadataCreated);    

    //then Update the assigned value to non accepted values
    testMetadataCreated.getManagedAttribute().get(0).setAssignedValue("c");
    Assertions.assertThrows(IllegalArgumentException.class, () -> objectStoreMetaDataService.update(testMetadataCreated));        

    testMetadataCreated.getManagedAttribute().get(0).getManagedAttribute().setManagedAttributeType(ManagedAttributeType.INTEGER);
    Assertions.assertThrows(IllegalArgumentException.class, () -> objectStoreMetaDataService.update(testMetadataCreated));        
  }  

}
