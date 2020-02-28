package ca.gc.aafc.objectstore.api.crud;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import ca.gc.aafc.objectstore.api.entities.DcType;


public class DcTypeTest {
  
  private DcType testDcType= DcType.MOVING_IMAGE;
  private DcType testDcType2= DcType.DATASET;
  
  @Test
  public void fromValue_whenInputWithSingleString_thenReturnProperDcType() {
    assertEquals(testDcType2,DcType.fromValue("dataset").get()); 
  }

  @Test
  public void fromValue_whenInputValueContainsNonAlpha_thenReturnProperDcType() {
    assertEquals(testDcType,DcType.fromValue("moving_image").get());    
    assertEquals(testDcType,DcType.fromValue("moving image").get());
  }
  
  @Test
  public void fromValue_whenInputValueNull_thenReturnEmptyDcType() {
    assertEquals(Optional.empty(),DcType.fromValue(null));
    
  }
}
