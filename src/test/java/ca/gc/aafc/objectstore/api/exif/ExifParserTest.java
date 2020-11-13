package ca.gc.aafc.objectstore.api.exif;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.Optional;

class ExifParserTest {

  public static final ClassPathResource TEST_PIC = new ClassPathResource("testPic.JPG");

  @SneakyThrows
  @Test
  void parseDate() {
    Optional<String> date = ExifParser.parseDate(ExifParser.parse(TEST_PIC.getFile()));
    Assertions.assertTrue(date.isPresent());
    Assertions.assertEquals("2018:12:12 14:28:34", date.get());
  }

}