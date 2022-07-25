package ca.gc.aafc.objectstore.api.repository;

import ca.gc.aafc.dina.exception.UnknownAttributeException;
import ca.gc.aafc.objectstore.api.config.MediaTypeConfiguration;
import ca.gc.aafc.objectstore.api.dto.MediaTypeDto;
import io.crnk.core.engine.internal.utils.PropertyException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ReadOnlyResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MediaTypeRepository extends ReadOnlyResourceRepositoryBase<MediaTypeDto, String> {

  private final Set<MediaTypeDto> mediaTypeSet;

  protected MediaTypeRepository(MediaTypeConfiguration mediaTypeConfig) {
    super(MediaTypeDto.class);
    mediaTypeSet = mediaTypeConfig.getSupportedMediaType().stream().map(MediaTypeDto::fromMediaType)
            .collect(Collectors.toSet());
  }

  @Override
  public ResourceList<MediaTypeDto> findAll(QuerySpec query) {
    try {
      return query.apply(mediaTypeSet);
    } catch (PropertyException propertyException) {
      throw new UnknownAttributeException(propertyException);
    }
  }
}
