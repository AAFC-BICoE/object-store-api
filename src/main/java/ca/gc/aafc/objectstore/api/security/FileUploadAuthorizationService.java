package ca.gc.aafc.objectstore.api.security;

import ca.gc.aafc.dina.entity.DinaEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class FileUploadAuthorizationService {

  @PreAuthorize("hasMinimumGroupAndRolePermissions(@currentUser, 'GUEST', #entity)")
  public void authorizeUpload(ObjectUploadAuth entity) {
  }

  /**
   * Wrapper class to allow usage of regular dina authorization service.
   */
  public record ObjectUploadAuth(String group) implements DinaEntity {

    @Override
    public Integer getId() {
      return null;
    }

    @Override
    public UUID getUuid() {
      return null;
    }

    @Override
    public String getGroup() {
      return group;
    }

    @Override
    public String getCreatedBy() {
      return null;
    }

    @Override
    public OffsetDateTime getCreatedOn() {
      return null;
    }
  }
}
