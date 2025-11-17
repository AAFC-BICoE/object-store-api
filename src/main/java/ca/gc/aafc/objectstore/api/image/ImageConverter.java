package ca.gc.aafc.objectstore.api.image;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * Utility class for converting images using ImageMagick 7.
 *
 * <p>This class provides a wrapper around the ImageMagick command-line tool to perform
 * image conversion operations such as rotation and quality adjustment. ImageMagick 7
 * must be installed and available in the system PATH for this class to function.</p>
 */
@Log4j2
public final class ImageConverter {

  private static final String IMAGE_MAGICK_COMMAND = "magick";
  private static final int DEFAULT_TIMEOUT_SECONDS = 60;

  /**
   * Lazy-initialized cached result of the tool availability check.
   */
  private static final LazyInitializer<Boolean> TOOL_AVAILABLE = new LazyInitializer<>() {
    @Override
    protected Boolean initialize() {
      return checkToolAvailability();
    }
  };

  private ImageConverter() {
    //utility class
  }

  /**
   * Checks if ImageMagick 7 is available on the system.
   *
   * @return {@code true} if ImageMagick is available and responds successfully to the
   * version command; {@code false} otherwise (including if initialization fails)
   */
  public static boolean isToolAvailable() {
    try {
      return TOOL_AVAILABLE.get();
    } catch (ConcurrentException e) {
      log.error("Failed to check tool availability", e);
      return false;
    }
  }

  /**
   * Internal method to perform the actual check to determine if ImageMagick is available.
   * @return {@code true} if the ImageMagick command executes successfully with exit code 0;
   *         {@code false} if the command fails or throws an {@link IOException}
   */
  private static boolean checkToolAvailability() {
    CommandLine cmdLine = new CommandLine(IMAGE_MAGICK_COMMAND);
    cmdLine.addArgument("-version");

    DefaultExecutor executor = DefaultExecutor.builder().get();
    try {
      int exitValue = executor.execute(cmdLine);
      log.info("ImageMagick is available");
      return exitValue == 0;
    } catch (IOException e) {
      log.warn("ImageMagick not available: {}", e.getMessage());
      return false;
    }
  }

  /**
   * Converts an image using ImageMagick with the specified options.
   *
   * <p>This method wraps ImageMagick's command-line interface to perform image conversion
   * operations. It supports options specified in the {@link ImageConversionOptions}.</p>
   *
   * @param inputPath              the file system path to the input image (must not be {@code null})
   *                               Considered safe so should be validated before calling this method to prevent command injection attacks
   * @param outputPath             the file system path where the converted image will be saved
   *                               (must not be {@code null})
   * @param imageConversionOptions the conversion options including quality and rotation
   *                               (must not be {@code null})
   */
  public static boolean convert(String inputPath, String outputPath, ImageConversionOptions imageConversionOptions) throws IOException {

    if (!isToolAvailable()) {
      throw new IllegalStateException(
        "Tool with command " + IMAGE_MAGICK_COMMAND + " not available");
    }

    CommandLine cmdLine = new CommandLine(IMAGE_MAGICK_COMMAND);

    cmdLine.addArgument(inputPath, false);
    if (imageConversionOptions.getRotation() != null && imageConversionOptions.getRotation() != 0) {
      cmdLine.addArgument("-rotate");
      cmdLine.addArgument(String.valueOf(imageConversionOptions.getRotation()), false);
    }

    if (!Objects.isNull(imageConversionOptions.getQuality())) {
      cmdLine.addArgument("-quality");
      cmdLine.addArgument(String.valueOf(imageConversionOptions.getQuality()), false);
    }

    cmdLine.addArgument(outputPath);

    DefaultExecutor executor = DefaultExecutor.builder().get();
    ExecuteWatchdog watchdog = ExecuteWatchdog.builder().setTimeout(Duration.of(DEFAULT_TIMEOUT_SECONDS,
      ChronoUnit.SECONDS)).get();
    executor.setWatchdog(watchdog);

    try {
      int exitValue = executor.execute(cmdLine);
      return exitValue == 0;
    } catch (ExecuteException e) {
      throw new IOException("Execution failed: " + e.getMessage(), e);
    }
  }

  @Builder
  @Getter
  public static class ImageConversionOptions {

    /**
     * "For the JPEG and MPEG image formats, quality is 1 (lowest image quality and highest compression) to 100 (best quality but least effective compression)."
     * see <a href="https://imagemagick.org/script/command-line-options.php#quality">...</a>
     */
    private Integer quality;

    /**
     * Number of degrees to rotate
     */
    @Builder.Default
    private Integer rotation = 0;

  }
}
