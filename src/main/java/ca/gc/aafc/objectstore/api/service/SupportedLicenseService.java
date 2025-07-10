package ca.gc.aafc.objectstore.api.service;

import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.service.CollectionBackedReadOnlyDinaService;
import ca.gc.aafc.objectstore.api.SupportedLicensesConfiguration;
import ca.gc.aafc.objectstore.api.dto.LicenseDto;

@Service
public class SupportedLicenseService extends CollectionBackedReadOnlyDinaService<String, LicenseDto> {

  public SupportedLicenseService(SupportedLicensesConfiguration licensesConfig) {
    super(licensesConfig.getLicenses().values(), LicenseDto::getId);
  }
}

