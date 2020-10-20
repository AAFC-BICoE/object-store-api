package ca.gc.aafc.objectstore.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import ca.gc.aafc.dina.DinaBaseApiAutoConfiguration;
import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.mapper.CustomFieldResolverSpec;
import ca.gc.aafc.dina.mapper.JpaDtoMapper;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.resolvers.ObjectStoreMetaDataFieldResolvers;
import ca.gc.aafc.objectstore.api.respository.DtoEntityMapping;
import io.minio.MinioClient;

@Configuration
@EntityScan("ca.gc.aafc.objectstore.api.entities")
@ComponentScan(basePackageClasses = DinaBaseApiAutoConfiguration.class)
@ImportAutoConfiguration(DinaBaseApiAutoConfiguration.class)
public class MainConfiguration {

  @Inject
  private ObjectStoreMetaDataFieldResolvers metaDataFieldResolver;

  @Bean
  @ConditionalOnMissingBean
  public MinioClient initMinioClient(
      @Value("${minio.scheme:}") String protocol, 
      @Value("${minio.host:}") String host,
      @Value("${minio.port:}") int port, 
      @Value("${minio.accessKey:}") String accessKey, 
      @Value("${minio.secretKey:}") String secretKey) {
    String endpoint = protocol + "://" + host;
    return MinioClient.builder()
        .endpoint(endpoint, port, false)
        .credentials(accessKey, secretKey).build();
  }

  /**
   * Configures DTO-to-Entity mappings.
   * 
   * @return the DtoJpaMapper
   */
  @Bean
  public JpaDtoMapper dtoJpaMapper(BaseDAO baseDAO) {
    Map<Class<?>, List<CustomFieldResolverSpec<?>>> customFieldResolvers = new HashMap<>();

    // Add custom field mapping for ObjectStoreMetadata DTO and Entity
    customFieldResolvers.put(ObjectStoreMetadataDto.class, metaDataFieldResolver.getDtoResolvers());
    customFieldResolvers.put(ObjectStoreMetadata.class, metaDataFieldResolver.getEntityResolvers());

    return new JpaDtoMapper(
      DtoEntityMapping.getDtoToEntityMapping(ObjectStoreMetadataDto.class),
      customFieldResolvers
    );
  }

}
