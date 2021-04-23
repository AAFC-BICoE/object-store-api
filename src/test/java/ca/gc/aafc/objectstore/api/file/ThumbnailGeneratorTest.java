package ca.gc.aafc.objectstore.api.file;

import com.google.common.io.Resources;
import org.apache.tika.mime.MimeTypeException;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class ThumbnailGeneratorTest {

  private static final MediaTypeDetectionStrategy MTDS = new MediaTypeDetectionStrategy();

  @Test
  public void thumbnailSupported_whenCr2_isSupportedIsFalse() throws URISyntaxException, MimeTypeException, IOException {
    try (FileInputStream fis = new FileInputStream(
        Resources.getResource("sample2CR2.CR2").toURI().getPath())) {

      MediaTypeDetectionStrategy.MediaTypeDetectionResult mtdr = MTDS.detectMediaType(fis,
          "image/x-canon-cr2", "sample2CR2.CR2");

      assertFalse(ThumbnailGenerator.isSupported(mtdr.getDetectedMediaType().getType()));
    }
  }
}
