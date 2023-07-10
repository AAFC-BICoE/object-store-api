package ca.gc.aafc.objectstore.api.testsupport.factories;

import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.web.MockMultipartFile;

public class MultipartFileFactory {

  public static MockMultipartFile createMockMultipartFile(ResourceLoader resourceLoader,
                                                          String fileNameInClasspath,
                                                          String mediaType
  ) throws IOException {
    Resource testFile = resourceLoader.getResource("classpath:" + fileNameInClasspath);
    byte[] bytes = IOUtils.toByteArray(testFile.getInputStream());
    return new MockMultipartFile("file",
      "testfile" + "." + FilenameUtils.getExtension(fileNameInClasspath), mediaType, bytes);
  }
}
