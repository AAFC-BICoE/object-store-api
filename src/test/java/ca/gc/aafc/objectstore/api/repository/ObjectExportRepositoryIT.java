package ca.gc.aafc.objectstore.api.repository;

import java.util.List;
import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import ca.gc.aafc.dina.testsupport.security.WithMockKeycloakUser;
import ca.gc.aafc.dina.util.UUIDHelper;
import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.dto.ObjectExportDto;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ObjectExportRepositoryIT extends BaseIntegrationTest {

  @Inject
  private ObjectExportRepository repository;

  @WithMockKeycloakUser(groupRole = "test:user")
  @Test
  public void objectExport_onNonExistingUUID_exceptionThrown() {
    ObjectExportDto dto = ObjectExportDto.builder()
      .fileIdentifiers(List.of(UUIDHelper.generateUUIDv7())).build();
    assertThrows(InvalidDataAccessApiUsageException.class, () -> repository.create(dto));
  }
}
