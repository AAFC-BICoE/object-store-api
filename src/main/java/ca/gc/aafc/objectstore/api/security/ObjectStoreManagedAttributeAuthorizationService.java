package ca.gc.aafc.objectstore.api.security;

import ca.gc.aafc.dina.security.PermissionAuthorizationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class ObjectStoreManagedAttributeAuthorizationService extends PermissionAuthorizationService {

  @Override
  @PreAuthorize("hasMinimumDinaRole(@currentUser, 'SUPER_USER')")
  public void authorizeCreate(Object entity) {
  }

  @Override
  @PreAuthorize("hasMinimumDinaRole(@currentUser, 'SUPER_USER')")
  public void authorizeUpdate(Object entity) {
  }

  @Override
  @PreAuthorize("hasMinimumDinaRole(@currentUser, 'SUPER_USER')")
  public void authorizeDelete(Object entity) {
  }

  @Override
  public void authorizeRead(Object entity) {
  }

  @Override
  public String getName() {
    return "ObjectStoreManagedAttributeAuthorizationService";
  }
}
