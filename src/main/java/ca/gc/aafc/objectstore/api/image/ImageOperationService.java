package ca.gc.aafc.objectstore.api.image;

import com.twelvemonkeys.image.ResampleOp;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FileUtils;
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

  /**
   * Converts an image from one format to another using ImageMagick.
   *
   * @param source the input image stream
   * @param sourceExtension the file extension of the source image (e.g., ".cr2")
   * @param out the output stream where the converted image will be written
   * @param destinationExtension the file extension of the destination format (e.g., ".tiff")
   * @param quality the quality setting for the output image, or null for default
   * @param rotation the rotation angle in degrees, or null for no rotation
   * @throws IOException if an I/O error occurs during conversion or file operations
   */
  public void magick(InputStream source, String sourceExtension, OutputStream out,
                     String destinationExtension, Integer quality, Integer rotation)
      throws IOException {

    var options = ImageConverter.ImageConversionOptions.builder()
      .quality(quality)
      .rotation(rotation)
      .build();

    // For magick delegate we need files on disk
    Path magickWorkingTmpFolder = Files.createTempDirectory("");
    try {
      Path sourcePath = Files.createTempFile(magickWorkingTmpFolder, "", sourceExtension);
      Files.copy(source, sourcePath, StandardCopyOption.REPLACE_EXISTING);

      Path destinationPath = Files.createTempFile(magickWorkingTmpFolder, "", destinationExtension);
      ImageConverter.convert(sourcePath.toString(), destinationPath.toString(), options);
      Files.copy(destinationPath, out);
    } finally {
      // Cleanup: use FileUtils or recursive delete
      FileUtils.deleteDirectory(magickWorkingTmpFolder.toFile());
    }
  }
}
