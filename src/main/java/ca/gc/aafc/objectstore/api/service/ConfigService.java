package ca.gc.aafc.objectstore.api.service;

import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.service.CollectionBackedReadOnlyDinaService;
import ca.gc.aafc.objectstore.api.DefaultValueConfiguration;
import ca.gc.aafc.objectstore.api.config.FileUploadConfiguration;
import ca.gc.aafc.objectstore.api.dto.ConfigPropertiesDto;

import java.util.List;

@Service
public class ConfigService extends CollectionBackedReadOnlyDinaService<String, ConfigPropertiesDto> {

  public ConfigService(FileUploadConfiguration fileConfig,
                       DefaultValueConfiguration defaultConfig) {
    super( List.of(
      ConfigPropertiesDto.builder().id("file-upload").properties(fileConfig.getMultipart()).build(),
      ConfigPropertiesDto.builder().id("default-values").properties(defaultConfig.getProperties()).build()),
      ConfigPropertiesDto::getId);
  }
}
