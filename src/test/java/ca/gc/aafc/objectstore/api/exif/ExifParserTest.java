package ca.gc.aafc.objectstore.api.exif;

import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class ExifParserTest {

  @SneakyThrows
  @Test
  void parse() {
    Metadata meta = ExifParser.parse(new ClassPathResource("drawing.png").getFile());
    Assertions.assertFalse(meta.hasErrors());

    for (Directory directory : meta.getDirectories()) {
      System.out.println(directory.getName());
      for (Tag tag : directory.getTags()) {
        System.out.println(tag.getTagName() + ":" + tag.getDescription());
      }
    }
  }


}