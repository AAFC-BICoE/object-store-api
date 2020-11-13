package ca.gc.aafc.objectstore.api.exif;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Parse EXIF data from an InputStream and extract relevant data.
 */
@Log4j2
public final class ExifParser {

  public static final List<String> DATE_TAKEN_POSSIBLE_TAGS = List.of(
      "Date/Time Original",
      "Date/Time",
      "Date/Time Digitized");

  private ExifParser() {
  }

  /**
   * Try to extract EXIF data from the provided {@link InputStream}.
   * If the format is unsupported and empty map is returned.
   * @param inputStream {@link InputStream} to parse, can't ne null, won't be closed.
   * @return all exif found returned as key/value or empty map if can't be extracted.
   * @throws IOException
   */
  public static Map<String, String> extractExifTags(@NonNull InputStream inputStream) throws IOException {
    Metadata metadata;
    try {
      metadata = ImageMetadataReader.readMetadata(inputStream);
    } catch (ImageProcessingException e) {
      log.debug(e);
      return Collections.emptyMap();
    }

    ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
    if (directory == null) {
      return Collections.emptyMap();
    }

    return directory.getTags().stream().collect(Collectors.toMap(Tag::getTagName, Tag::getDescription));

  }


}
