package ca.gc.aafc.objectstore.api.security;

import ca.gc.aafc.dina.entity.DinaEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

/***
 * Authorization for file upload.
 */
@Service
public class FileControllerAuthorizationService {

  @PreAuthorize("hasMinimumGroupAndRolePermissions(@currentUser, 'GUEST', #entity)")
  public void authorizeUpload(FileObjectAuth entity) {
  }

  @PreAuthorize("hasMinimumGroupAndRolePermissions(@currentUser, 'READ_ONLY', #entity)" +
    " || #entity.isPubliclyReleasable().orElse(false)")
  public void authorizeDownload(DinaEntity entity) {
  }

  @PreAuthorize("hasMinimumGroupAndRolePermissions(@currentUser, 'SUPER_USER', #entity)")
  public void authorizeFileInfo(FileObjectAuth entity) {
  }

  /**
   * Creates a {@link FileObjectAuth} from a bucket name.
   * @param bucket
   * @return
   */
  public static FileObjectAuth objectUploadAuthFromBucket(String bucket) {
    return new FileObjectAuth(bucket);
  }

  /**
   * Wrapper class (record) to allow usage of regular dina authorization service.
   * in the context of File Object (e.g. upload) the bucket represents the group.
   */
  public record FileObjectAuth(String group) implements DinaEntity {

    @Override
    public Integer getId() {
      return null;
    }

    @Override
    public UUID getUuid() {
      return null;
    }

    /**
     * We need to return the group using getGroup to follow Java Bean convention.
     * @return
     */
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
