package ca.gc.aafc.objectstore.api.exif;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.util.Optional;

class ExifParserTest {

  private static final ClassPathResource TEST_PIC = new ClassPathResource("testPic.jpg");
  private static final ClassPathResource TEST_FILE = new ClassPathResource("testfile.txt");

  @SneakyThrows
  @Test
  void extractDateTaken_OnEXIFAvailable_EXIFExtracted() {
    Optional<String> date = ExifParser.extractDateTaken(new FileInputStream(TEST_PIC.getFile()));
    Assertions.assertTrue(date.isPresent());
    Assertions.assertEquals("2020:11:13 10:03:17", date.get());
  }

  @SneakyThrows
  @Test
  void extractDateTaken_NoEXIFAvailable_NothingReturned() {
    Optional<String> date = ExifParser.extractDateTaken(new FileInputStream(TEST_FILE.getFile()));
    Assertions.assertTrue(date.isEmpty());
  }

}
