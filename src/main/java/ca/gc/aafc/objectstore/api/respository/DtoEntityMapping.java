package ca.gc.aafc.objectstore.api.respository;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ca.gc.aafc.dina.dto.RelatedEntity;
import ca.gc.aafc.dina.util.ClassAnnotationHelper;

public final class DtoEntityMapping {

  /**
   * Utility class
   */
  private DtoEntityMapping() {}
  
  /**
   * Get the mapping between dto and entity classes.
   * 
   * @param basePackage class of the package to scan. The idea is to have a valid class instead of a string.
   * @return
   */
  public static Map<Class<?>, Class<?>> getDtoToEntityMapping(Class<?> basePackage) {
    Map<Class<?>, Class<?>> dtoToEntityMapping = new HashMap<>();
    Set<Class<?>> classList = ClassAnnotationHelper.findAnnotatedClasses(basePackage, RelatedEntity.class);

    for (Class<?> currClass : classList) {
      dtoToEntityMapping.put(currClass, currClass.getAnnotation(RelatedEntity.class).value());
    }

    return dtoToEntityMapping;
  }

}
