package ca.gc.aafc.objectstore.api.exif;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.util.Map;
import java.util.Optional;

/**
 * Tests related to {@link ExifParser}
 */
class ExifParserTest {

  private static final ClassPathResource TEST_PIC = new ClassPathResource("testPic.jpg");
  private static final ClassPathResource TEST_FILE = new ClassPathResource("testfile.txt");

  @SneakyThrows
  @Test
  void extractDateTaken_OnEXIFAvailable_EXIFExtracted() {
    try (FileInputStream fis = new FileInputStream(TEST_PIC.getFile())) {
      Map<String, String> exifData = ExifParser.extractExifTags(fis);

      Optional<String> dateValue = exifData.keySet().stream()
          .filter(ExifParser.DATE_TAKEN_POSSIBLE_TAGS::contains)
          .findFirst();
      Assertions.assertTrue(dateValue.isPresent());
      Assertions.assertEquals("2020:11:13 10:03:17", exifData.get(dateValue.get()));
    }

  }

  @SneakyThrows
  @Test
  void extractDateTaken_NoEXIFAvailable_NothingReturned() {
    try (FileInputStream fis = new FileInputStream(TEST_FILE.getFile())) {
      Map<String, String> exifData = ExifParser.extractExifTags(fis);
      Assertions.assertTrue(exifData.isEmpty());
    }
  }

}
