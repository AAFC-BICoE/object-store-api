package ca.gc.aafc.objectstore.api.exif;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.util.Optional;

class ExifParserTest {

  public static final ClassPathResource TEST_PIC = new ClassPathResource("testPic.JPG");

  @SneakyThrows
  @Test
  void parseDate() {
    Optional<String> date = ExifParser.parseDate(ExifParser.parse(new FileInputStream(TEST_PIC.getFile())));
    Assertions.assertTrue(date.isPresent());
    Assertions.assertEquals("2020:11:13 10:03:17", date.get());
  }

}