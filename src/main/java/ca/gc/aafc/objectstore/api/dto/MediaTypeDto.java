package ca.gc.aafc.objectstore.api.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.tika.mime.MediaType;

import com.toedter.spring.hateoas.jsonapi.JsonApiId;
import com.toedter.spring.hateoas.jsonapi.JsonApiTypeForClass;

import ca.gc.aafc.dina.dto.JsonApiResource;

@JsonApiTypeForClass(MediaTypeDto.TYPENAME)
@Data
@AllArgsConstructor
public class MediaTypeDto implements JsonApiResource {

  public static final String TYPENAME = "media-type";

  @JsonApiId
  private String id;

  private String mediaType;

  public static MediaTypeDto fromMediaType(MediaType mt) {
    return new MediaTypeDto(mt.toString(), mt.toString());
  }

  @Override
  public String getJsonApiType() {
    return TYPENAME;
  }

  @Override
  public UUID getJsonApiId() {
    return null;
  }
}
