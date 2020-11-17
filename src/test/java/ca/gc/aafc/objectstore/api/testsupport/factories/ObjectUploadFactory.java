package ca.gc.aafc.objectstore.api.testsupport.factories;

import ca.gc.aafc.dina.testsupport.factories.TestableEntityFactory;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

public class ObjectUploadFactory implements TestableEntityFactory<ObjectUpload> {

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
        .fileIdentifier(UUID.randomUUID())
        .originalFilename("testFile")
        .sha1Hex("b52c98d49782a6ebb9c8e3bb1ad7aa2f03706481")
        .bucket("testBucket")
        .exif(Map.of("ex1", "exVal1"))
        .createdBy("createdBy");
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
