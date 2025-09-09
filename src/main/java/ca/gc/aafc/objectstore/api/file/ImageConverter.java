package ca.gc.aafc.objectstore.api.file;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import lombok.Builder;
import lombok.Getter;

/**
 * Requires ImageMagick 7 to be available to container/system running this code.
 */
public class ImageConverter {

  private static final String IMAGE_MAGICK_COMMAND = "magick";

  public static boolean convert(String inputPath, String outputPath, ImageConversionOptions imageConversionOptions) throws IOException {

    CommandLine cmdLine = new CommandLine(IMAGE_MAGICK_COMMAND);

    cmdLine.addArgument(inputPath);
    if (imageConversionOptions.getRotation() != 0) {
      cmdLine.addArgument("-rotate");
      cmdLine.addArgument(String.valueOf(imageConversionOptions.getRotation()));
    }

    cmdLine.addArgument("-quality");
    cmdLine.addArgument(String.valueOf(imageConversionOptions.getQuality()));

    cmdLine.addArgument(outputPath);

    DefaultExecutor executor = DefaultExecutor.builder().get();
    ExecuteWatchdog watchdog = ExecuteWatchdog.builder().setTimeout(Duration.of(60,
      ChronoUnit.SECONDS)).get(); // 60 seconds timeout
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

    private Integer quality;
    private Integer rotation;

  }
}
