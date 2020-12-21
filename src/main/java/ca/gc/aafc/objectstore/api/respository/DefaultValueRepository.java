package ca.gc.aafc.objectstore.api.respository;

import ca.gc.aafc.objectstore.api.ObjectStoreConfiguration;
import ca.gc.aafc.objectstore.api.dto.DefaultValuesDto;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ReadOnlyResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultValueRepository extends ReadOnlyResourceRepositoryBase<DefaultValuesDto, String> {

  private final DefaultValuesDto defaultResource;

  protected DefaultValueRepository(ObjectStoreConfiguration configuration) {
    super(DefaultValuesDto.class);
    defaultResource = new DefaultValuesDto();
    defaultResource.setId(1);
    defaultResource.setDefaultCopyright(configuration.getDefaultCopyright());
    defaultResource.setDefaultLicenceURL(configuration.getDefaultLicenceURL());
    defaultResource.setDefaultCopyrightOwner(configuration.getDefaultCopyrightOwner());
    defaultResource.setDefaultUsageTerms(configuration.getDefaultUsageTerms());
  }

  @Override
  public ResourceList<DefaultValuesDto> findAll(QuerySpec querySpec) {
    return querySpec.apply(List.of(defaultResource));
  }

  @Override
  public DefaultValuesDto findOne(String id, QuerySpec querySpec) {
    return defaultResource;
  }
}
