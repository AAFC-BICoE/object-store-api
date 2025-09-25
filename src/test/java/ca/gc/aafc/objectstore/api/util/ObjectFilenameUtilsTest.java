package ca.gc.aafc.objectstore.api.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectFilenameUtilsTest {

  @Test
  public void testStandardizeFilenameReplace() {
    assertEquals("1_2.jpg", ObjectFilenameUtils.standardizeFilename("1|2.jpg"));
    assertEquals("1_2.jpg", ObjectFilenameUtils.standardizeFilename("1?2.jpg"));
    assertEquals("abc_test.jpg", ObjectFilenameUtils.standardizeFilename("abc/test.jpg"));

    // allowed characters
    assertEquals("abc+test.jpg", ObjectFilenameUtils.standardizeFilename("abc+test.jpg"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"test.jpg", "testé.jpg", "test(1).jpg", "test 1.jpg"})
  public void testStandardizeFilenameKeep(String filename) {
    assertEquals(filename, ObjectFilenameUtils.standardizeFilename(filename));
  }
}
