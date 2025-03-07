package ca.gc.aafc.objectstore.api.dto;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonApiResource(type = ObjectExportDto.TYPENAME)
public class ObjectExportDto {

  public static final String TYPENAME = "object-export";

  @JsonApiId
  private UUID uuid;

  private List<UUID> fileIdentifiers;
  private Map<UUID, String> filenameAliases;
  private Map<String, List<UUID>> exportLayout;
  private String name;

}
