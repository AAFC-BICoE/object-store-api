package ca.gc.aafc.objectstore.api;

import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.exif.ExifParser;
import ca.gc.aafc.objectstore.api.file.FolderStructureStrategy;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;

import io.minio.MinioClient;

import org.springframework.http.MediaType;

import java.util.Map;
import java.util.UUID;

/**
 * 
 * Configuration used to override bean in the context of Integration testing.
 * A MinioClient stub with 1 entry will be created for testing purpose (see {@link #setupFile(MinioClient)})
 *
 */
@org.springframework.boot.test.context.TestConfiguration
public class MinioTestConfiguration {

  public static final String TEST_BUCKET = "test";
  public static final String TEST_USAGE_TERMS = "test usage terms";

  public static final UUID TEST_FILE_IDENTIFIER = UUID.randomUUID();
  public static final String TEST_FILE_EXT = ".txt";
  public static final String TEST_FILE_MEDIA_TYPE = MediaType.TEXT_PLAIN_VALUE;
  public static final String TEST_ORIGINAL_FILENAME = "myfile" + TEST_FILE_EXT;
  public static final String ILLEGAL_BUCKET_CHAR = "~";

  public static final String TEST_XMP_RIGHTS_WEB_STATEMENT = "https://open.canada.ca/en/open-government-licence-canada";
  public static final String TEST_XMP_RIGHTS_OWNER = "Government of Canada";
  public static final String TEST_XMP_RIGHTS_USAGE_TERMS = "Government of Canada Usage Terms";
  public static final String TEST_DC_RIGHTS = "Copyright Government of Canada";

  /**
   * Bu the ObjectUpload matching the one stored in the mock Minio.
   * @return
   */
  public static ObjectUpload buildTestObjectUpload() {
    return ObjectUploadFactory.newObjectUpload()
        .fileIdentifier(MinioTestConfiguration.TEST_FILE_IDENTIFIER)
        .evaluatedMediaType(MinioTestConfiguration.TEST_FILE_MEDIA_TYPE)
        .detectedMediaType(MinioTestConfiguration.TEST_FILE_MEDIA_TYPE)
        .detectedFileExtension(MinioTestConfiguration.TEST_FILE_EXT)
        .evaluatedFileExtension(MinioTestConfiguration.TEST_FILE_EXT)
        .bucket(MinioTestConfiguration.TEST_BUCKET)
        .originalFilename(MinioTestConfiguration.TEST_ORIGINAL_FILENAME)
        .exif(Map.of(ExifParser.DATE_TAKEN_POSSIBLE_TAGS.get(0), "2020:11:13 10:03:17"))
        .build();
  }

}
