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

  /**
   * Externally hosted resources to include in the export. These are resolved from their
   * metadata uuid, the backing resourceExternalURL is owned by the server.
   */
  private List<ExternalResource> externalResources;

  private ExportFunction exportFunction;
  private String name;

  @JsonIgnore
  private String username;

  /**
   * A single externally hosted resource to include in an export.
   * <p>
   * The metadata is identified by its uuid (not by a fileIdentifier). The optional filenameAlias
   * and folder only apply to this external item and do not affect stored files or derivatives.
   */
  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ExternalResource {
    private UUID metadataUuid;
    private String filenameAlias;
    private String folder;
  }

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
