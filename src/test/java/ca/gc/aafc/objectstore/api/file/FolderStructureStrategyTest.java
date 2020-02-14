package ca.gc.aafc.objectstore.api.file;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * Tests related to {@link FolderStructureStrategy}
 *
 */
public class FolderStructureStrategyTest {
  
  private static final FolderStructureStrategy FOLDER_STRUCT_STRATEGY = new FolderStructureStrategy();
  
  @Test
  public void getPathFor_onUUID_pathReturned() {
    UUID uuid = UUID.randomUUID();
    String filename = uuid.toString() + ".txt";
    
    Path generatedPath = FOLDER_STRUCT_STRATEGY.getPathFor(filename);
    
    // Path should be relative
    assertFalse(generatedPath.isAbsolute());
    
    // at least one folder should be generated
    assertNotNull(generatedPath.getParent());
    
  }

}
