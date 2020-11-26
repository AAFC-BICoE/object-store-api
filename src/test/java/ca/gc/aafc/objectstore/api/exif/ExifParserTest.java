package ca.gc.aafc.objectstore.api.exif;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests related to {@link ExifParser}
 */
class ExifParserTest {

  private static final ClassPathResource TEST_PIC = new ClassPathResource("testPic.jpg");
  private static final ClassPathResource TEST_FILE = new ClassPathResource("testfile.txt");

  @SneakyThrows
  @Test
  void extractExifTags_OnEXIFAvailable_EXIFExtracted() {
    try (FileInputStream fis = new FileInputStream(TEST_PIC.getFile())) {
      Map<String, String> exifData = ExifParser.extractExifTags(fis);

      Optional<String> dateValue = exifData.keySet().stream()
          .filter(ExifParser.DATE_TAKEN_POSSIBLE_TAGS::contains)
          .findFirst();
      Assertions.assertTrue(dateValue.isPresent());
      assertEquals("2020:11:13 10:03:17", exifData.get(dateValue.get()));
    }
  }

  @SneakyThrows
  @Test
  void extractExifTags_NoEXIFAvailable_NothingReturned() {
    try (FileInputStream fis = new FileInputStream(TEST_FILE.getFile())) {
      Map<String, String> exifData = ExifParser.extractExifTags(fis);
      Assertions.assertTrue(exifData.isEmpty());
    }
  }

  @Test
  void parseDateTaken_WhenDateAvailable_dateParsedAndReturned() {
    Map<String, String> exif = Map.of(ExifParser.DATE_TAKEN_POSSIBLE_TAGS.get(0), "2020:11:13 10:03:17");
    assertEquals(Optional.of(LocalDateTime.of(2020,11,13,10,3,17)),
        ExifParser.parseDateTaken(exif));
  }

}
