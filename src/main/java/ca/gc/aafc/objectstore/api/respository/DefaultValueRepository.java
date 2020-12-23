package ca.gc.aafc.objectstore.api.respository;

import ca.gc.aafc.objectstore.api.DefaultValueConfiguration;
import ca.gc.aafc.objectstore.api.dto.DefaultValuesDto;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ReadOnlyResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DefaultValueRepository extends ReadOnlyResourceRepositoryBase<DefaultValuesDto, String> {

  private final List<DefaultValuesDto> defaultValuesList;

  protected DefaultValueRepository(DefaultValueConfiguration configuration) {
    super(DefaultValuesDto.class);
    List<DefaultValuesDto> list = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(configuration.getValues())) {
      for (DefaultValueConfiguration.DefaultValue defaultValue : configuration.getValues()) {
        DefaultValuesDto build = DefaultValuesDto.builder()
          .id(defaultValue.getType() + "-" + defaultValue.getAttribute())
          .value(defaultValue.getValue())
          .type(defaultValue.getType())
          .attribute(defaultValue.getAttribute())
          .build();
        list.add(build);
      }
    }
    defaultValuesList = list;
  }

  @Override
  public ResourceList<DefaultValuesDto> findAll(QuerySpec querySpec) {
    return querySpec.apply(defaultValuesList);
  }

  @Override
  public DefaultValuesDto findOne(String id, QuerySpec querySpec) {
    return defaultValuesList.stream()
      .filter(dto -> dto.getId().equalsIgnoreCase(id))
      .findAny()
      .orElseThrow(() -> new ResourceNotFoundException("Could not find a default value with id " + id));
  }
}
