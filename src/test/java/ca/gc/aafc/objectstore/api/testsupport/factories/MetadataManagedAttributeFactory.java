package ca.gc.aafc.objectstore.api.testsupport.factories;

import ca.gc.aafc.dina.testsupport.factories.TestableEntityFactory;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import org.apache.commons.lang3.RandomStringUtils;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;

public class MetadataManagedAttributeFactory implements TestableEntityFactory<MetadataManagedAttribute> {
  
  private static final ZoneId MTL_TZ = ZoneId.of("America/Montreal");
  private static final ZonedDateTime TEST_ZONED_DT = ZonedDateTime.of(2019, 1, 2, 3, 4, 5, 0, MTL_TZ);
  private static final OffsetDateTime TEST_OFFSET_DT = TEST_ZONED_DT.toOffsetDateTime();  

  @Override
  public MetadataManagedAttribute getEntityInstance() {
    return newMetadataManagedAttribute().build();
  }
  /**
   * Static method that can be called to return a configured builder that can be further customized
   * to return the actual entity object, call the .build() method on a builder.
   * 
   * @return Pre-configured builder with all mandatory fields set
   */
  public static MetadataManagedAttribute.MetadataManagedAttributeBuilder newMetadataManagedAttribute() {
    return MetadataManagedAttribute.builder()
      .uuid(UUID.randomUUID())
      .objectStoreMetadata(ObjectStoreMetadataFactory.newObjectStoreMetadata()
        .acDigitizationDate(TEST_OFFSET_DT).build())
      .managedAttribute(ManagedAttributeFactory.newManagedAttribute()
        .acceptedValues(new String[]{"a", "b", "test value"}).build())
      .assignedValue("test value")
      .createdBy(RandomStringUtils.random(4));
   } 
  
  /**
   * A utility method to create a list of qty number of Chains with no configuration.
   * 
   * @param qty The number of Chains populated in the list
   * @return List of Chain
   */
  public static List<MetadataManagedAttribute> newListOf(int qty) {
    return newListOf(qty, null);
  }

  /**
   * A utility method to create a list of qty number of Chain with an incrementing attribute
   * based on the configuration argument. An example of configuration would be the functional
   * interface (bldr, index) -> bldr.name(" string" + index)
   * 
   * @param qty           The number of Chain that is populated in the list.
   * @param configuration the function to apply, usually to differentiate the different entities in
   *                      the list.
   * @return List of Chain
   */
  public static List<MetadataManagedAttribute> newListOf(int qty,
      BiFunction<MetadataManagedAttribute.MetadataManagedAttributeBuilder, Integer, MetadataManagedAttribute.MetadataManagedAttributeBuilder> configuration) {
    
    return TestableEntityFactory.newEntity(qty, MetadataManagedAttributeFactory::newMetadataManagedAttribute, configuration,
        MetadataManagedAttribute.MetadataManagedAttributeBuilder::build);
  }
  

}
