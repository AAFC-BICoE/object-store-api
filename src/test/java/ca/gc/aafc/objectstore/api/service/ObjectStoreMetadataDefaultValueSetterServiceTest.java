package ca.gc.aafc.objectstore.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.gc.aafc.objectstore.api.MediaTypeToDcTypeConfiguration;
import ca.gc.aafc.objectstore.api.ObjectStoreConfiguration;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;

public class ObjectStoreMetadataDefaultValueSetterServiceTest {
  
  private ObjectStoreMetadataDefaultValueSetterService serviceUnderTest;

  @BeforeEach
  public void setup() {
    ObjectStoreConfiguration config = new ObjectStoreConfiguration("a", "b", "c");
    
    LinkedHashMap<String, LinkedList<String>> toDcType = new LinkedHashMap<>();
    LinkedList<String> patterns = new LinkedList<>();
    patterns.add("^image\\/[\\w\\.-]+$");
    toDcType.put("IMAGE", patterns);
    
    patterns = new LinkedList<>();
    patterns.add("^text/csv$");
    toDcType.put("DATASET", patterns);
    
    MediaTypeToDcTypeConfiguration dcTypeConfig = new MediaTypeToDcTypeConfiguration();
    dcTypeConfig.setToDcType(toDcType);
    dcTypeConfig = new MediaTypeToDcTypeConfiguration();
    dcTypeConfig.setToDcType(toDcType);
    
    serviceUnderTest = new ObjectStoreMetadataDefaultValueSetterService(config, dcTypeConfig);
  }
  
  @Test
  public void assignDefaultValues_onNoDcFormat_DcTypeIsUndetermined() {
    ObjectStoreMetadata osmd = new ObjectStoreMetadata();
    serviceUnderTest.assignDefaultValues(osmd);
    assertEquals(DcType.UNDETERMINED, osmd.getDcType());
  }
  
  @Test
  public void assignDefaultValues_onPngDcFormat_DcTypeIsImage() {
    ObjectStoreMetadata osmd = new ObjectStoreMetadata();
    osmd.setDcFormat("image/png");
    serviceUnderTest.assignDefaultValues(osmd);
    assertEquals(DcType.IMAGE, osmd.getDcType());
  }

}
