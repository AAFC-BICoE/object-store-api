package ca.gc.aafc.objectstore.api.repository;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.dto.LicenseDto;
import ca.gc.aafc.objectstore.api.repository.SupportedLicenseRepository;
import io.crnk.core.queryspec.QuerySpec;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SupportedLicenseRepositoryIT extends BaseIntegrationTest {

  @Inject
  private SupportedLicenseRepository supportedLicenseRepository;

  @Test
  public void findAllLicense_whenFindAll_licensesReturned() {
    List<LicenseDto> licenseList = supportedLicenseRepository
        .findAll(new QuerySpec(LicenseDto.class));
    assertNotNull(licenseList);
    assertNotNull(licenseList.get(0));
  }


}
