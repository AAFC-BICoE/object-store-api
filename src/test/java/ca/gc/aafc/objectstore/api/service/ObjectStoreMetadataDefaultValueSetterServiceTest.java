package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.objectstore.api.DefaultValueConfiguration;
import ca.gc.aafc.objectstore.api.MediaTypeToDcTypeConfiguration;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ObjectStoreMetadataDefaultValueSetterServiceTest {

  private ObjectStoreMetadataDefaultValueSetterService serviceUnderTest;
  public static final String EXPECTED_LICENCE_VALUE = "test-licence";
  public static final String EXPECTED_COPYRIGHT = "test-COPYRIGHT";
  public static final String EXPECTED_USAGE_TERM = "test-USAGE";
  public static final String EXPECTED_OWNER = "test-OWNER";
  public static final List<DefaultValueConfiguration.DefaultValue> VALUES = List.of(
    new DefaultValueConfiguration.DefaultValue(ObjectStoreMetadataDto.TYPENAME, "xmpRightsWebStatement", EXPECTED_LICENCE_VALUE),
    new DefaultValueConfiguration.DefaultValue(ObjectStoreMetadataDto.TYPENAME, "dcRights", EXPECTED_COPYRIGHT),
    new DefaultValueConfiguration.DefaultValue(ObjectStoreMetadataDto.TYPENAME, "xmpRightsOwner", EXPECTED_OWNER),
    new DefaultValueConfiguration.DefaultValue(ObjectStoreMetadataDto.TYPENAME, "xmpRightsUsageTerms", EXPECTED_USAGE_TERM)
  );

  @BeforeEach
  public void setup() {
    DefaultValueConfiguration config = new DefaultValueConfiguration();
    config.setValues(VALUES);

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
  public void assignDefaultValues_onDefaultValues_ValuesSet() {
    ObjectStoreMetadata osmd = new ObjectStoreMetadata();
    serviceUnderTest.assignDefaultValues(osmd);
    assertEquals(DcType.UNDETERMINED, osmd.getDcType());
    assertEquals(EXPECTED_LICENCE_VALUE, osmd.getXmpRightsWebStatement());
    assertEquals(EXPECTED_COPYRIGHT, osmd.getDcRights());
    assertEquals(EXPECTED_USAGE_TERM, osmd.getXmpRightsUsageTerms());
    assertEquals(EXPECTED_OWNER, osmd.getXmpRightsOwner());
  }

  @Test
  public void assignDefaultValues_onPngDcFormat_DcTypeIsImage() {
    ObjectStoreMetadata osmd = new ObjectStoreMetadata();
    osmd.setDcFormat("image/png");
    serviceUnderTest.assignDefaultValues(osmd);
    assertEquals(DcType.IMAGE, osmd.getDcType());
  }

  @Test
  public void assignDefaultValues_onNoDcFormat_DcTypeIsUndetermined() {
    ObjectStoreMetadata osmd = new ObjectStoreMetadata();
    serviceUnderTest.assignDefaultValues(osmd);
    assertEquals(DcType.UNDETERMINED, osmd.getDcType());
  }

  @Test
  public void assignDefaultValues_onDcTypeImage_PubliclyReleasableCorrectlySet() {
    ObjectStoreMetadata osmd = new ObjectStoreMetadata();
    osmd.setDcType(DcType.IMAGE);
    serviceUnderTest.assignDefaultValues(osmd);
    assertTrue(osmd.getPubliclyReleasable());

    osmd = new ObjectStoreMetadata();
    osmd.setDcType(DcType.UNDETERMINED);
    serviceUnderTest.assignDefaultValues(osmd);
    assertFalse(osmd.getPubliclyReleasable());
  }

}
