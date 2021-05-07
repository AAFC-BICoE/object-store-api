package ca.gc.aafc.objectstore.api.repository;

import ca.gc.aafc.objectstore.api.DefaultValueConfiguration;
import ca.gc.aafc.objectstore.api.FileUploadConfiguration;
import ca.gc.aafc.objectstore.api.dto.ConfigPropertiesDto;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ReadOnlyResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConfigRepository extends ReadOnlyResourceRepositoryBase<ConfigPropertiesDto, String> {

  private final List<ConfigPropertiesDto> resources;

  protected ConfigRepository(
    FileUploadConfiguration fileConfig,
    DefaultValueConfiguration defaultConfig
  ) {
    super(ConfigPropertiesDto.class);
    this.resources = List.of(
      ConfigPropertiesDto.builder().id("file-upload").properties(fileConfig.getMultipart()).build(),
      ConfigPropertiesDto.builder().id("default-values").properties(defaultConfig.getProperties()).build()
    );
  }

  @Override
  public ResourceList<ConfigPropertiesDto> findAll(QuerySpec querySpec) {
    return querySpec.apply(resources);
  }
}
