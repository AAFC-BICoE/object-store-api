package ca.gc.aafc.objectstore.api.image;

import com.twelvemonkeys.image.ResampleOp;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.springframework.stereotype.Service;

/**
 * Service class responsible to apply operations/manipulations on images.
 */
@Service
public class ImageOperationService {

  /**
   * Resize the provided {@link BufferedImage} with a factor.
   * @param original
   * @param factor
   * @return
   */
  public BufferedImage resize(BufferedImage original, float factor) {
    BufferedImageOp
      resampler = new ResampleOp((int) (original.getWidth() * factor),
      (int) (original.getHeight() * factor), ResampleOp.FILTER_LANCZOS);
    return resampler.filter(original, null);
  }

  public void magick(InputStream source, String sourceExtension, OutputStream out,
                     String destinationExtension, Integer quality, Integer rotation)
      throws IOException {

    var options = ImageConverter.ImageConversionOptions.builder()
      .quality(quality)
      .rotation(rotation)
      .build();

    // For magick delegate we need files on disk
    Path magickWorkingTmpFolder = Files.createTempDirectory("");
    Path sourcePath = Files.createTempFile(magickWorkingTmpFolder,"", sourceExtension);
    Files.copy(source, sourcePath, StandardCopyOption.REPLACE_EXISTING);
    Path destinationPath = Files.createTempFile(magickWorkingTmpFolder,"", destinationExtension);
    ImageConverter.convert(sourcePath.toString(), destinationPath.toString(), options);

    Files.copy(destinationPath, out);

    // cleanup
    sourcePath.toFile().delete();
    destinationPath.toFile().delete();
  }
}
