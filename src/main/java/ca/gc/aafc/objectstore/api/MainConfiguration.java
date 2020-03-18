package ca.gc.aafc.objectstore.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import ca.gc.aafc.dina.DinaBaseApiAutoConfiguration;
import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.mapper.JpaDtoMapper;
import ca.gc.aafc.dina.repository.SelectionHandler;
import ca.gc.aafc.objectstore.api.dto.AgentDto;
import ca.gc.aafc.objectstore.api.dto.ManagedAttributeDto;
import ca.gc.aafc.objectstore.api.dto.MetadataManagedAttributeDto;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.dto.ObjectSubtypeDto;
import ca.gc.aafc.objectstore.api.entities.Agent;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import io.minio.MinioClient;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;

@Configuration
@EntityScan("ca.gc.aafc.objectstore.api.entities")
@ComponentScan(basePackageClasses = DinaBaseApiAutoConfiguration.class)
@ImportAutoConfiguration(DinaBaseApiAutoConfiguration.class)
public class MainConfiguration {
  
  @Bean
  @Profile("!test")
  public MinioClient initMinioClient(
      @Value("${minio.scheme:}") String protocol, 
      @Value("${minio.host:}") String host,
      @Value("${minio.port:}") int port, 
      @Value("${minio.accessKey:}") String accessKey, 
      @Value("${minio.secretKey:}") String secretKey)
      throws InvalidEndpointException, InvalidPortException {
    String endpoint = protocol + "://"+host;
    return new MinioClient(endpoint, port, accessKey, secretKey);
  }

  /**
   * Configures DTO-to-Entity mappings.
   * 
   * @return the DtoJpaMapper
   */
  @Bean
  public JpaDtoMapper dtoJpaMapper(SelectionHandler selectionHandler, BaseDAO baseDAO) {
    Map<Class<?>, Class<?>> jpaEntities = new HashMap<>();
    Map<Class<?>, List<JpaDtoMapper.CustomFieldResolverSpec<?>>> customFieldResolvers = new HashMap<>();

    jpaEntities.put(AgentDto.class, Agent.class);
    jpaEntities.put(ManagedAttributeDto.class, ManagedAttribute.class);
    jpaEntities.put(MetadataManagedAttributeDto.class, MetadataManagedAttribute.class);
    jpaEntities.put(ObjectStoreMetadataDto.class, ObjectStoreMetadata.class);
    jpaEntities.put(ObjectSubtypeDto.class, ObjectSubtype.class);
    
    return new JpaDtoMapper(jpaEntities, customFieldResolvers, selectionHandler, baseDAO);
  }

}
