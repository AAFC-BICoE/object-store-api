package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute.ManagedAttributeType;
import ca.gc.aafc.objectstore.api.exif.ExifParser;
import ca.gc.aafc.objectstore.api.testsupport.factories.ManagedAttributeFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.MetadataManagedAttributeFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.impl.matchers.AndMatcher;

import javax.inject.Inject;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectStoreMetadataServiceIT extends BaseIntegrationTest {

  @Inject
  private ObjectStoreMetaDataService objectStoreMetaDataService;
  private ObjectStoreMetadata testMetadataCreated;
    
  @BeforeEach
  void setup(){
    ObjectStoreMetadata objectStoreMetadata = ObjectStoreMetadataFactory.newObjectStoreMetadata()
        .build();

    ManagedAttribute ma1 = ManagedAttributeFactory.newManagedAttribute()
        .acceptedValues(new String[]{"a", "b"}).build();

    ManagedAttribute ma2 = ManagedAttributeFactory.newManagedAttribute()
        .acceptedValues(new String[]{"1", "2"}).build();

    MetadataManagedAttribute metadataMA = MetadataManagedAttribute.builder()
        .uuid(UUID.randomUUID())
        .objectStoreMetadata(objectStoreMetadata)
        .managedAttribute(ma1)
        .assignedValue("a")
        .createdBy(RandomStringUtils.random(4)).build();

    MetadataManagedAttribute metadataMA2 = MetadataManagedAttribute.builder()
        .uuid(UUID.randomUUID())
        .objectStoreMetadata(objectStoreMetadata)
        .managedAttribute(ma2)
        .assignedValue("1")
        .createdBy(RandomStringUtils.random(4)).build();        

    List<MetadataManagedAttribute> metadataMAList = new ArrayList<MetadataManagedAttribute>();
    metadataMAList.add(metadataMA);
    metadataMAList.add(metadataMA2);

    ObjectStoreMetadata testMetadata = ObjectStoreMetadataFactory.newObjectStoreMetadata()
    .managedAttribute(metadataMAList)
    .build();

    testMetadataCreated = objectStoreMetaDataService.create(testMetadata);
    Assertions.assertNotNull(testMetadataCreated);    
  }

  @Test
  public void objectStoreMetaDataService_OnUpdate_InvalidMetadataManagedAttributeValues_throwException() {
    //then Update the assigned value to non accepted values
    testMetadataCreated.getManagedAttribute().stream().forEach(
      metaMA -> {
        if (metaMA.getManagedAttribute().getManagedAttributeType().compareTo(ManagedAttributeType.STRING) == 0 ) {
          metaMA.setAssignedValue("c");
          Assertions.assertThrows(IllegalArgumentException.class, () -> objectStoreMetaDataService.update(testMetadataCreated));        
        } else if (metaMA.getManagedAttribute().getManagedAttributeType().compareTo(ManagedAttributeType.INTEGER) == 1) {
          metaMA.setAssignedValue("3");
          Assertions.assertThrows(IllegalArgumentException.class, () -> objectStoreMetaDataService.update(testMetadataCreated));        
        }
      }
    );       
  }  

  @Test
  public void objectStoreMetaDataService_OnUpdate_ValidMetadataManagedAttributeValues_ValuesUpdated() {
    //then Update the assigned value to non accepted values
    testMetadataCreated.getManagedAttribute().stream().forEach(
      metaMA -> {
        if (metaMA.getManagedAttribute().getManagedAttributeType().compareTo(ManagedAttributeType.STRING) == 0 ) {
          metaMA.setAssignedValue("b");
          service.runInNewTransaction(entityManager -> {
            entityManager.merge(testMetadataCreated);
          });          
          ObjectStoreMetadata testMetadataUpdated = objectStoreMetaDataService.findOne(testMetadataCreated.getUuid(), ObjectStoreMetadata.class);
         assertTrue(testMetadataUpdated.getManagedAttribute().stream().map(MetadataManagedAttribute::getAssignedValue).anyMatch(metaMA.getAssignedValue()::equals));

        } else if (metaMA.getManagedAttribute().getManagedAttributeType().compareTo(ManagedAttributeType.INTEGER) == 1) {
          metaMA.setAssignedValue("2");
          service.runInNewTransaction(entityManager -> {
            entityManager.merge(testMetadataCreated);
          });          
          ObjectStoreMetadata testMetadataUpdated = objectStoreMetaDataService.findOne(testMetadataCreated.getUuid(), ObjectStoreMetadata.class);
          assertTrue(testMetadataUpdated.getManagedAttribute().stream().map(MetadataManagedAttribute::getAssignedValue).anyMatch(metaMA.getAssignedValue()::equals));
        }
      }
    );       
  }    

}
