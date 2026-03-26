package ca.gc.aafc.objectstore.api.config;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Configuration;

import ca.gc.aafc.dina.dto.ApiInfoDto;
import ca.gc.aafc.objectstore.api.image.ImageConverter;

@Configuration
public class ApiInfoConfiguration {

  @Value("${dina.fileStorage.implementation:}")
  private String storageImplementation;

  @Value("${dina.messaging.isProducer:false}")
  private Boolean isProducer;

  @Value("${dina.messaging.isConsumer:false}")
  private Boolean isConsumer;

  private final String apiVersion;
  private final boolean magickCommandAvailable;

  public ApiInfoConfiguration(BuildProperties buildProperties) {
    this.apiVersion = buildProperties.getVersion();
    this.magickCommandAvailable = ImageConverter.isToolAvailable();
  }

  public ApiInfoDto buildApiInfoDto() {
    ApiInfoDto infoDto = new ApiInfoDto();
    infoDto.setModuleVersion(apiVersion);
    infoDto.setMessageProducer(isProducer);
    infoDto.setMessageConsumer(isConsumer);

    Map<String, Object> moduleInfo = Map.of(
      "storageImplementation", storageImplementation,
      "magickCommandAvailable", magickCommandAvailable);

    infoDto.setModuleInfo(moduleInfo);
    return infoDto;
  }
}
