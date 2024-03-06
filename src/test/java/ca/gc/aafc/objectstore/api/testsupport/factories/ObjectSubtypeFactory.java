package ca.gc.aafc.objectstore.api.testsupport.factories;

import java.util.List;
import java.util.function.BiFunction;

import ca.gc.aafc.dina.testsupport.factories.TestableEntityFactory;
import ca.gc.aafc.dina.util.UUIDHelper;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import ca.gc.aafc.objectstore.api.entities.DcType;

public class ObjectSubtypeFactory implements TestableEntityFactory<ObjectSubtype> {

  @Override
  public ObjectSubtype getEntityInstance() {
    return newObjectSubtype().build();
  }
  
  /**
   * Static method that can be called to return a configured builder that can be further customized
   * to return the actual entity object, call the .build() method on a builder.
   * 
   * @return Pre-configured builder with all mandatory fields set
   */
  public static ObjectSubtype.ObjectSubtypeBuilder newObjectSubtype() {
    return ObjectSubtype.builder()
        .uuid(UUIDHelper.generateUUIDv7())
        .createdBy("createdBy")
        .dcType(DcType.IMAGE)
        .acSubtype(TestableEntityFactory.generateRandomNameLettersOnly(12).toUpperCase());
   } 
  
  /**
   * A utility method to create a list of qty number of ObjectSubtype with no configuration.
   * 
   * @param qty The number of ObjectSubtype populated in the list
   * @return List of ObjectSubtype
   */
  public static List<ObjectSubtype> newListOf(int qty) {
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
  public static List<ObjectSubtype> newListOf(int qty,
      BiFunction<ObjectSubtype.ObjectSubtypeBuilder, Integer, ObjectSubtype.ObjectSubtypeBuilder> configuration) {
    
    return TestableEntityFactory.newEntity(qty, ObjectSubtypeFactory::newObjectSubtype, configuration,
        ObjectSubtype.ObjectSubtypeBuilder::build);
  }

}
