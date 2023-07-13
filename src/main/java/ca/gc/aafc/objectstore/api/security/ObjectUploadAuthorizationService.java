package ca.gc.aafc.objectstore.api.security;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.security.auth.PermissionAuthorizationService;

/**
 * Authorization for the resource representing the file upload.
 * ObjectUpload are immutable. We only allow modifications for administrative purpose.
 */
@Service
public class ObjectUploadAuthorizationService extends PermissionAuthorizationService {

  // Do not allow create. It is done through FileUpload
  @Override
  @PreAuthorize("denyAll")
  public void authorizeCreate(Object entity) {
  }

  @Override
  @PreAuthorize("hasDinaRole(@currentUser, 'DINA_ADMIN')")
  public void authorizeUpdate(Object entity) {
  }

  @Override
  @PreAuthorize("hasDinaRole(@currentUser, 'DINA_ADMIN')")
  public void authorizeDelete(Object entity) {
  }

  @Override
  public void authorizeRead(Object entity) {
  }

  @Override
  public String getName() {
    return "FileUploadResourceAuthorizationService";
  }
}

