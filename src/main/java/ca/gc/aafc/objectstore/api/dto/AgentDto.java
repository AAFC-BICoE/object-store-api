package ca.gc.aafc.objectstore.api.dto;

import java.util.UUID;

import ca.gc.aafc.dina.dto.RelatedEntity;
import ca.gc.aafc.objectstore.api.entities.Agent;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.Data;

/**
 * Temporary class holding Agent data until the module is ready.
 *
 */
@SuppressFBWarnings({ "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
@RelatedEntity(Agent.class)
@Data
@JsonApiResource(type = "agent")
public class AgentDto {
  
  @JsonApiId 
  private UUID uuid;

  private String displayName;
  private String email;
  
}
