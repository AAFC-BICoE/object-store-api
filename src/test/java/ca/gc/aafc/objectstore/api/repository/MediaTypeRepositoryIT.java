package ca.gc.aafc.objectstore.api.repository;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.dto.MediaTypeDto;
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
    List<MediaTypeDto> mediaTypeList = mediaTypeRepository.findAll("");
    assertNotNull(mediaTypeList);
    assertNotNull(mediaTypeList.getFirst());
  }

  @Test
  public void findMediaTypeList_onFilter_MediaTypeReturned() {
    // dina-base 0.144 required
    List<MediaTypeDto> mediaTypeList = mediaTypeRepository.findAll("filter[mediaType][LIKE]=%jpeg%");
    assertTrue(mediaTypeList.stream()
            .anyMatch(mt -> MediaType.IMAGE_JPEG_VALUE.equals(mt.getMediaType())));
  }
}
