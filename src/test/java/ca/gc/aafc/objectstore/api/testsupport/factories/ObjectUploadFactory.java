package ca.gc.aafc.objectstore.api.testsupport.factories;

import ca.gc.aafc.dina.testsupport.factories.TestableEntityFactory;
import ca.gc.aafc.dina.util.UUIDHelper;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.exif.ExifParser;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

import org.springframework.http.MediaType;

public class ObjectUploadFactory implements TestableEntityFactory<ObjectUpload> {

  public static final String TEST_BUCKET = "test";
  public static final String TEST_USAGE_TERMS = "test usage terms";

  public static final UUID TEST_FILE_IDENTIFIER = UUIDHelper.generateUUIDv7();
  public static final String TEST_FILE_EXT = ".txt";
  public static final String TEST_FILE_MEDIA_TYPE = MediaType.TEXT_PLAIN_VALUE;
  public static final String TEST_ORIGINAL_FILENAME = "myfile" + TEST_FILE_EXT;
  public static final String ILLEGAL_BUCKET_CHAR = "~";

  public static final String TEST_XMP_RIGHTS_WEB_STATEMENT = "https://open.canada.ca/en/open-government-licence-canada";
  public static final String TEST_XMP_RIGHTS_OWNER = "Government of Canada";
  public static final String TEST_XMP_RIGHTS_USAGE_TERMS = "Government of Canada Usage Terms";
  public static final String TEST_DC_RIGHTS = "Copyright Government of Canada";
  
  @Override
  public ObjectUpload getEntityInstance() {
    return newObjectUpload().build();
  }

  /**
   * Static method that can be called to return a configured builder that can be further customized
   * to return the actual entity object, call the .build() method on a builder.
   *
   * @return Pre-configured builder with all mandatory fields set
   */
  public static ObjectUpload.ObjectUploadBuilder newObjectUpload() {
    return ObjectUpload.builder()
        .fileIdentifier(UUIDHelper.generateUUIDv7())
        .dcType(DcType.IMAGE)
        .originalFilename("testFile")
        .sha1Hex("b52c98d49782a6ebb9c8e3bb1ad7aa2f03706481")
        .bucket("testbucket")
        .exif(Map.of("ex1", "exVal1"))
        .evaluatedFileExtension(".jpg")
        .isDerivative(false)
        .detectedMediaType("image/jpeg")
        .evaluatedMediaType("image/jpeg")
        .createdBy("createdBy");
  }
  
  /**
   * Bu the ObjectUpload matching the one stored in the mock Minio.
   * @return
   */
  public static ObjectUpload buildTestObjectUpload() {
    return newObjectUpload()
        .fileIdentifier(TEST_FILE_IDENTIFIER)
        .evaluatedMediaType(TEST_FILE_MEDIA_TYPE)
        .detectedMediaType(TEST_FILE_MEDIA_TYPE)
        .detectedFileExtension(TEST_FILE_EXT)
        .evaluatedFileExtension(TEST_FILE_EXT)
        .bucket(TEST_BUCKET)
        .originalFilename(TEST_ORIGINAL_FILENAME)
        .exif(Map.of(ExifParser.DATE_TAKEN_POSSIBLE_TAGS.get(0), "2020:11:13 10:03:17"))
        .build();
  }

  /**
   * A utility method to create a list of qty number of ObjectSubtype with no configuration.
   *
   * @param qty The number of ObjectSubtype populated in the list
   * @return List of ObjectSubtype
   */
  public static List<ObjectUpload> newListOf(int qty) {
    return newListOf(qty, null);
  }

  /**
   * A utility method to create a list of qty number of ObjectSubtype with an incrementing attribute
   * based on the configuration argument. An example of configuration would be the functional
   * interface (bldr, index) -> bldr.name(" string" + index)
   *
   * @param qty           The number of ObjectSubtype that is populated in the list.
   * @param configuration the function to apply, usually to differentiate the different entities in
   *                      the list.
   * @return List of ObjectSubtype
   */
  public static List<ObjectUpload> newListOf(int qty,
                                             BiFunction<ObjectUpload.ObjectUploadBuilder, Integer, ObjectUpload.ObjectUploadBuilder> configuration) {

    return TestableEntityFactory.newEntity(qty, ObjectUploadFactory::newObjectUpload, configuration,
        ObjectUpload.ObjectUploadBuilder::build);
  }
}
