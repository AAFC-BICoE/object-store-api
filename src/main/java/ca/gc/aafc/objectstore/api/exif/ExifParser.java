package ca.gc.aafc.objectstore.api.exif;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import org.apache.commons.lang3.StringUtils;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Parse EXIF data from an InputStream and extract relevant data.
 */
@Log4j2
public final class ExifParser {

  public static final DateTimeFormatter EXIF_DATE_FORMATTER = DateTimeFormatter.ofPattern("uuuu:MM:dd HH:mm:ss");
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
    return directory.getTags().stream().filter(tag -> StringUtils.isNotBlank(tag.getDescription()))
      .collect(Collectors.toMap(Tag::getTagName, Tag::getDescription));
  }

  /**
   * Tries to parse a {@link LocalDateTime} from EXIF tags.
   * @param exif
   * @return
   */
  public static Optional<LocalDateTime> parseDateTaken(@NonNull Map<String, String> exif) {
    for (Map.Entry<String, String> eData : exif.entrySet()) {
      if (DATE_TAKEN_POSSIBLE_TAGS.contains(eData.getKey())) {
        try {
          return Optional.of(LocalDateTime.parse(eData.getValue(), EXIF_DATE_FORMATTER));
        } catch (DateTimeParseException dtpEx) { //if we can't parse it we just skip
          log.debug(dtpEx);
        }
      }
    }
    return Optional.empty();
  }


}
