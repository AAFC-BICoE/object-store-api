package ca.gc.aafc.objectstore.api.testsupport.factories;

import java.util.List;

import ca.gc.aafc.dina.i18n.MultilingualDescription;
import ca.gc.aafc.dina.testsupport.factories.TestableEntityFactory;

public class MultilingualDescriptionFactory implements TestableEntityFactory<MultilingualDescription> {

  @Override
  public MultilingualDescription getEntityInstance() {
    return newMultilingualDescription().build();
  }
  
  /**
   * Static method that can be called to return a configured builder that can be further customized
   * to return the actual entity object, call the .build() method on a builder.
   * 
   * @return Pre-configured builder with all mandatory fields set
   */
  public static MultilingualDescription.MultilingualDescriptionBuilder newMultilingualDescription() {
    return MultilingualDescription.builder()
            .descriptions(List.of(
              MultilingualDescription.MultilingualPair.of("en", "attrEn"),
              MultilingualDescription.MultilingualPair.of("fr", "attrFr")
            ));
   } 
  
}
