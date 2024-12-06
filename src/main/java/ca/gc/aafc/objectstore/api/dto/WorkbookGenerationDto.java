package ca.gc.aafc.objectstore.api.dto;

import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import com.toedter.spring.hateoas.jsonapi.JsonApiId;
import com.toedter.spring.hateoas.jsonapi.JsonApiTypeForClass;

@Data
@Builder
@Jacksonized
@JsonApiTypeForClass(WorkbookGenerationDto.TYPE)
public class WorkbookGenerationDto {

  public static final String TYPE = "workbook-generation";

  @JsonApiId
  private UUID id;

  private List<String> columns;
  private List<String> aliases;

}
