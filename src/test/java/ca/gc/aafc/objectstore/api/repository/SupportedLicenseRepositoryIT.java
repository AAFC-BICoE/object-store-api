package ca.gc.aafc.objectstore.api.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ca.gc.aafc.dina.exception.UnknownAttributeException;
import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.dto.LicenseDto;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import javax.inject.Inject;

public class SupportedLicenseRepositoryIT extends BaseIntegrationTest {

  @Inject
  private SupportedLicenseRepository supportedLicenseRepository;

  @Test
  public void findAllLicense_whenFindAll_licensesReturned() {
    List<LicenseDto> licenseList = supportedLicenseRepository
        .findAll("");
    assertNotNull(licenseList);
    assertNotNull(licenseList.get(0));
  }

  @Test
  public void findAllLicense_withNonExistentField_UnknownAttributeExceptionThrown() {
    Assertions.assertThrows(UnknownAttributeException.class,
      () -> supportedLicenseRepository
      .findAll("sort=createdOn"));
  }

}
