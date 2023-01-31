package ca.gc.aafc.objectstore.api.security;

import ca.gc.aafc.dina.security.PermissionAuthorizationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class MetadataAuthorizationService extends PermissionAuthorizationService {

  @Override
  @PreAuthorize("hasMinimumGroupAndRolePermissions(@currentUser, 'GUEST', #entity)")
  public void authorizeCreate(Object entity) {
  }

  @Override
  @PreAuthorize("hasMinimumGroupAndRolePermissions(@currentUser, 'GUEST', #entity)")
  public void authorizeUpdate(Object entity) {
  }

  /**
   * Let super-user deal with delete since deleting things from the object-store is not common.
   * @param entity
   */
  @Override
  @PreAuthorize("hasMinimumGroupAndRolePermissions(@currentUser, 'SUPER_USER', #entity)")
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
