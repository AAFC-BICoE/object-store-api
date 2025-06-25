package ca.gc.aafc.objectstore.api.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.toedter.spring.hateoas.jsonapi.JsonApiId;
import com.toedter.spring.hateoas.jsonapi.JsonApiTypeForClass;

import ca.gc.aafc.objectstore.api.config.ExportFunction;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonApiTypeForClass(ObjectExportDto.TYPENAME)
public class ObjectExportDto implements ca.gc.aafc.dina.dto.JsonApiResource {

  public static final String TYPENAME = "object-export";

  @JsonApiId
  private UUID uuid;

  private List<UUID> fileIdentifiers;
  private Map<UUID, String> filenameAliases;
  private Map<String, List<UUID>> exportLayout;
  private ExportFunction exportFunction;
  private String name;

  @JsonIgnore
  private String username;

  @JsonIgnore
  @Override
  public String getJsonApiType() {
    return TYPENAME;
  }

  @JsonIgnore
  @Override
  public UUID getJsonApiId() {
    return uuid;
  }
}
