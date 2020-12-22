package ca.gc.aafc.objectstore.api.respository;

import ca.gc.aafc.objectstore.api.FileUploadConfiguration;
import ca.gc.aafc.objectstore.api.dto.FileUploadConfigDto;
import ca.gc.aafc.objectstore.api.dto.LicenseDto;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ReadOnlyResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.List;

@Component
public class FileConfigRepository extends ReadOnlyResourceRepositoryBase<FileUploadConfigDto, String> {

  private final FileUploadConfigDto resource;

  protected FileConfigRepository(@NotNull FileUploadConfiguration config) {
    super(FileUploadConfigDto.class);
    this.resource = FileUploadConfigDto.builder()
      .id("1")
      .maxFileSize(config.getMaxFileSize())
      .maxRequestSize(config.getMaxRequestSize())
      .build();
  }

  @Override
  public ResourceList<FileUploadConfigDto> findAll(QuerySpec querySpec) {
    return querySpec.apply(List.of(resource));
  }
}
