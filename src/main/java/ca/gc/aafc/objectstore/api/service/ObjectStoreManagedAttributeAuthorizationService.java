package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.dina.service.DinaAuthorizationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class ObjectStoreManagedAttributeAuthorizationService implements DinaAuthorizationService {
  @Override
  @PreAuthorize("hasDinaRole(@currentUser, 'COLLECTION_MANAGER')")
  public void authorizeCreate(Object entity) {
  }

  @Override
  @PreAuthorize("hasDinaRole(@currentUser, 'COLLECTION_MANAGER')")
  public void authorizeUpdate(Object entity) {
  }

  @Override
  @PreAuthorize("hasDinaRole(@currentUser, 'COLLECTION_MANAGER')")
  public void authorizeDelete(Object entity) {
  }
}
