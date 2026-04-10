package ca.gc.aafc.objectstore.api.security;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class DinaAdminAuthorizationService {

  @PreAuthorize("hasAdminRole(@currentUser, 'DINA_ADMIN')")
  public void authorize() {
  }
}
