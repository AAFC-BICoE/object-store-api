package ca.gc.aafc.objectstore.api.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;


public class DcTypeTest {
  
  @Test
  public void fromValue_whenInputWithSingleString_thenReturnProperDcType() {
    assertEquals(DcType.DATASET,DcType.fromValue("dataset").get()); 
    assertEquals(DcType.DATASET,DcType.fromValue(DcType.DATASET.toString()).get()); 
  }

  @Test
  public void fromValue_whenInputValueContainsNonAlpha_thenReturnProperDcType() {
    assertEquals(DcType.MOVING_IMAGE,DcType.fromValue("moving_image").get());    
    assertEquals(DcType.MOVING_IMAGE,DcType.fromValue("moving image").get());
  }
  
  @Test
  public void fromValue_whenInputValueBlanck_thenReturnEmptyDcType() {
    assertEquals(Optional.empty(),DcType.fromValue(null));
    assertEquals(Optional.empty(),DcType.fromValue(""));
  }
}
