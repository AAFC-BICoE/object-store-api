package ca.gc.aafc.objectstore.api.dto;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.tika.mime.MediaType;

@JsonApiResource(type = "media-type")
@Data
@AllArgsConstructor
public class MediaTypeDto {

  @JsonApiId
  private String id;

  private String mediaType;

  public static MediaTypeDto fromMediaType(MediaType mt) {
    return new MediaTypeDto(mt.toString(), mt.toString());
  }

}
