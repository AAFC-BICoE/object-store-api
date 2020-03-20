package ca.gc.aafc.objectstore.api.respository;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import ca.gc.aafc.objectstore.api.dto.RelatedEntity;
import lombok.extern.log4j.Log4j2;

@Log4j2
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
    try {
      List<Class<?>> classList = findAnnotatedClasses(basePackage, RelatedEntity.class);
      
      for(Class<?> currClass : classList) {
        dtoToEntityMapping.put(currClass, currClass.getAnnotation(RelatedEntity.class).value());
      }
    } catch (ClassNotFoundException e) {
      // not really possible since the class is coming from a package scan
      log.error(e);
    }
    return dtoToEntityMapping;
  }
  
  /**
   * To be moved to dina-base-api.
   * Find all classes from package represented by the basePackage class that are annotated with the annotationClass.
   * 
   * @param basePackage
   * @param annotationClass
   * @return
   * @throws ClassNotFoundException
   */
  public static List<Class<?>> findAnnotatedClasses(Class<?> basePackage, Class<? extends Annotation> annotationClass) throws ClassNotFoundException {
    ClassPathScanningCandidateComponentProvider provider = newComponentScannerByAnnotation(annotationClass);
    List<Class<?>> classList = new ArrayList<>();
    for (BeanDefinition beanDef : provider.findCandidateComponents(basePackage.getPackage().getName())) {
      classList.add(Class.forName(beanDef.getBeanClassName()));
    }
    return classList;
  }

  /**
   * To be moved to dina-base-api
   * @param annotationClass
   * @return
   */
  private static ClassPathScanningCandidateComponentProvider newComponentScannerByAnnotation(Class<? extends Annotation> annotationClass) {
    // Don't extract default filters
    ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
        false);
    provider.addIncludeFilter(new AnnotationTypeFilter(annotationClass));
    return provider;
  }

}
