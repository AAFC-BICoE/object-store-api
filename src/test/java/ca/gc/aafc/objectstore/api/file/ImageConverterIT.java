package ca.gc.aafc.objectstore.api.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImageConverterIT {

  @Inject
  private ResourceLoader resourceLoader;

  private static final ClassPathResource TEST_IMG = new ClassPathResource("sample2CR2.CR2");

  @Test
  public void testImageConverter() throws IOException {
    if (ImageConverter.isToolAvailable()) {
      ImageConverter.ImageConversionOptions options =
        ImageConverter.ImageConversionOptions.builder()
          .quality(85)
          .rotation(90)
          .build();
      Path tmpFile = Files.createTempFile("tmp", ".jpeg");
      assertTrue(
        ImageConverter.convert(TEST_IMG.getFile().getPath(), tmpFile.toString(), options));
      tmpFile.toFile().deleteOnExit();
    } else {
      System.out.println("Skipping testImageConverter. ImageMagick 7 not available");
    }
  }
}
