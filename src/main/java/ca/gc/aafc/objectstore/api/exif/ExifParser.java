package ca.gc.aafc.objectstore.api.exif;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * Parse EXIF data from an InputStream and extract relevant data.
 */
@Log4j2
public final class ExifParser {

  private static final List<Integer> DATE_TAKEN_POSSIBLE_TAGS = List.of(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL,
    ExifSubIFDDirectory.TAG_DATETIME, ExifSubIFDDirectory.TAG_DATETIME_DIGITIZED);

  private ExifParser() {
  }

  /**
   * Try to extract the date from the EXIF in provided {@link InputStream}.
   * If the format is unsupported {@link Optional#empty()} is returned
   * @param inputStream
   * @return date as string or @link Optional#empty()} if can't be extracted.
   * @throws IOException
   */
  public static Optional<String> extractDateTaken(@NonNull InputStream inputStream) throws IOException {
    Metadata metadata;
    try {
      metadata = ImageMetadataReader.readMetadata(inputStream);
    } catch (ImageProcessingException e) {
      log.debug(e);
      return Optional.empty();
    }

    ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
    if(directory == null) {
      return Optional.empty();
    }

    for(Integer tag: DATE_TAKEN_POSSIBLE_TAGS) {
      if(StringUtils.isNotBlank(directory.getDescription(tag))) {
        return Optional.of(directory.getDescription(tag));
      }
    }
    return Optional.empty();
  }

}
