package ca.gc.aafc.objectstore.api.exif;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Iterator;
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
  public static final String UNKNOWN_TAG = "Unknown tag";
  public static final List<String> DATE_TAKEN_POSSIBLE_TAGS = List.of(
      "Date/Time Original",
      "Date/Time",
      "Date/Time Digitized");

  private ExifParser() {
  }

  /**
   * Try to extract EXIF data from the provided {@link InputStream}.
   * If the format is unsupported and empty map is returned.
   * @param inputStream {@link InputStream} to parse, can't be null, won't be closed.
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

    // we need to take ALL the directories of ExifSubIFDDirectory (not only the first one)
    List<ExifSubIFDDirectory> directories = List.copyOf(metadata.getDirectoriesOfType(ExifSubIFDDirectory.class));
    if (directories.isEmpty()) {
      return Collections.emptyMap();
    }

    Map<String, String> exif = new HashMap<>();
    for (Directory directory : directories) {
      for (Tag tag : directory.getTags()) {
        // skip empty values and Unknown tags
        if (StringUtils.isNotBlank(tag.getDescription()) && !tag.getTagName().startsWith(UNKNOWN_TAG)) {
          exif.put(tag.getTagName(), tag.getDescription());
        }
      }
    }
    return exif;
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
