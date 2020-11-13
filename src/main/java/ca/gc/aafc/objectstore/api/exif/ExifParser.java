package ca.gc.aafc.objectstore.api.exif;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import lombok.SneakyThrows;

import java.io.InputStream;
import java.util.Optional;

public final class ExifParser {

  private ExifParser() {
  }

  @SneakyThrows
  public static Metadata parse(InputStream inputStream) {
    return ImageMetadataReader.readMetadata(inputStream, 10 * 1024L);
  }

  public static Optional<String> parseDate(Metadata metadata) {
    ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
    if (directory != null) {
      String description = directory.getDescription(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
      if (description == null) {
        description = directory.getDescription(ExifSubIFDDirectory.TAG_DATETIME);
      }
      if (description == null) {
        description = directory.getDescription(ExifSubIFDDirectory.TAG_DATETIME_DIGITIZED);
      }
      return Optional.ofNullable(description);
    }
    return Optional.empty();
  }

}
