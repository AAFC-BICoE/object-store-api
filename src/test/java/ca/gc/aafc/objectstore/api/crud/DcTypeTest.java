package ca.gc.aafc.objectstore.api.crud;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import ca.gc.aafc.objectstore.api.ObjectStoreApiLauncher;
import ca.gc.aafc.objectstore.api.entities.DcType;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ObjectStoreApiLauncher.class)
@ActiveProfiles("test")
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
