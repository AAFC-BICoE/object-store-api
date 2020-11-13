package ca.gc.aafc.objectstore.api.exif;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import lombok.SneakyThrows;

import java.io.File;

public class ExifParser {

  @SneakyThrows
  public static Metadata parse(File inputStream){
    return ImageMetadataReader.readMetadata(inputStream);
  }

}
