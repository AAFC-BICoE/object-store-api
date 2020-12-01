package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.dina.jpa.BaseDAO;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MetaManagedAttributeServiceIT extends BaseIntegrationTest {

  @Inject
  private MetaManagedAttributeService metaManagedAttributeService;
  @Inject
  private BaseDAO baseDAO;
  private MetadataManagedAttribute testMetaManagedAttribute;
  private MetadataManagedAttribute testMetaManagedAttribute2;
    
  @BeforeEach
  void setup(){

    ObjectStoreMetadata testMetadata = ObjectStoreMetadataFactory.newObjectStoreMetadata()
        .build();
    
    ManagedAttribute ma1 = ManagedAttributeFactory.newManagedAttribute()
        .acceptedValues(new String[]{"a", "b"})
        .managedAttributeType(ManagedAttributeType.STRING).build();

    ManagedAttribute ma2 = ManagedAttributeFactory.newManagedAttribute()
        .acceptedValues(new String[]{"1", "2"})
        .managedAttributeType(ManagedAttributeType.INTEGER).build();

    testMetaManagedAttribute = MetadataManagedAttribute.builder()
        .uuid(UUID.randomUUID())
        .objectStoreMetadata(testMetadata)
        .managedAttribute(ma1)
        .assignedValue("a")
        .createdBy(RandomStringUtils.random(4)).build();

    testMetaManagedAttribute2 = MetadataManagedAttribute.builder()
        .uuid(UUID.randomUUID())
        .objectStoreMetadata(testMetadata)
        .managedAttribute(ma2)
        .assignedValue("1")
        .createdBy(RandomStringUtils.random(4)).build();       
    
    baseDAO.create(ma1);
    baseDAO.create(ma2);
    baseDAO.create(testMetadata);
  }

  @Test
  public void objectStoreMetaDataService_OnUpdate_InvalidMetadataManagedAttributeValues_throwException() {

    testMetaManagedAttribute.setAssignedValue("c");
    Assertions.assertThrows(IllegalArgumentException.class, () -> metaManagedAttributeService.update(testMetaManagedAttribute));        
    testMetaManagedAttribute2.setAssignedValue("3");
    Assertions.assertThrows(IllegalArgumentException.class, () -> metaManagedAttributeService.update(testMetaManagedAttribute2));        
    
  }  

  @Test
  public void objectStoreMetaDataService_OnUpdate_ValidMetadataManagedAttributeValues_valueUpdated() {

    testMetaManagedAttribute.setAssignedValue("b");
    MetadataManagedAttribute metaMA = metaManagedAttributeService.update(testMetaManagedAttribute);
    assertNotNull(metaMA);
    Assertions.assertEquals(metaMA.getAssignedValue(), testMetaManagedAttribute.getAssignedValue());        
    testMetaManagedAttribute2.setAssignedValue("1");
    metaMA = metaManagedAttributeService.update(testMetaManagedAttribute2);
    assertNotNull(metaMA);
    Assertions.assertEquals(metaMA.getAssignedValue(), testMetaManagedAttribute2.getAssignedValue());        
    
  }    
}
