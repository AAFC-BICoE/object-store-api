package ca.gc.aafc.objectstore.api.image;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import lombok.extern.log4j.Log4j2;

/**
 * Utility class for images read/write operations with usable default settings.
 */
@Log4j2
public final class ImageUtils {

  private static final ImageTypeSpecifier TYPE_WITH_ALPHA =
    ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB);

  private ImageUtils () {
    // no op
  }

  public static void writeJpeg(BufferedImage imgContent, ImageOutputStream output)
      throws IOException {

    Iterator<ImageWriter> jpegWriters = ImageIO.getImageWritersBySuffix("jpg");
    if (jpegWriters.hasNext()) {
      ImageWriter imageWriter = jpegWriters.next();

      if (imgContent.getColorModel().hasAlpha()) {
        if (!canEncodeImageWithAlpha(imageWriter)) {
          imageWriter.dispose();
          throw new IOException("ImageWriter can't handle images with an alpha channel");
        }
      }

      imageWriter.setOutput(output);
      imageWriter.write(imgContent);
      imageWriter.dispose();
    } else {
      log.error("No ImageWriter found for jpg");
    }
  }

  /**
   * Checks if a given ImageWriter can potentially handle images with an alpha channel.
   *
   * @param imageWriter The ImageWriter instance to check.
   * @return true if the writer's format provider indicates it can encode
   *         a standard ARGB image type, false otherwise.
   */
  private static boolean canEncodeImageWithAlpha(ImageWriter imageWriter) {
    var provider = imageWriter.getOriginatingProvider();
    return provider.canEncodeImage(TYPE_WITH_ALPHA);
  }
}
