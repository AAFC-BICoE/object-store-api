package ca.gc.aafc.objectstore.api.repository;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.dto.MediaTypeDto;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import javax.inject.Inject;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MediaTypeRepositoryIT extends BaseIntegrationTest {

  @Inject
  private MediaTypeRepository mediaTypeRepository;

  @Test
  public void findMediaTypeList_whenFindAll_mediaTypeListReturned() {
    List<MediaTypeDto> mediaTypeList = mediaTypeRepository
            .findAll(new QuerySpec(MediaTypeDto.class));
    assertNotNull(mediaTypeList);
    assertNotNull(mediaTypeList.get(0));
  }

  @Test
  public void findMediaTypeList_onFilter_MediaTypeReturned() {
    QuerySpec q = new QuerySpec(MediaTypeDto.class);
    q.addFilter(PathSpec.of("mediaType").filter(FilterOperator.LIKE, "%jpeg%"));
    List<MediaTypeDto> mediaTypeList = mediaTypeRepository.findAll(q);

    assertTrue(mediaTypeList.stream()
            .anyMatch(mt -> MediaType.IMAGE_JPEG_VALUE.equals(mt.getMediaType())));
  }


}
