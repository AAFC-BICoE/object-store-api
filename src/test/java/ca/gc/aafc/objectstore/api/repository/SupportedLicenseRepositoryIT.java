package ca.gc.aafc.objectstore.api.repository;

import ca.gc.aafc.dina.exception.UnknownAttributeException;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.dto.LicenseDto;
import ca.gc.aafc.objectstore.api.repository.SupportedLicenseRepository;
import io.crnk.core.queryspec.Direction;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.SortSpec;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import java.util.Collections;
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

  @Test
  public void findAllLicense_withNonExistantField_UnknownAttributeExceptionThrown() {
    QuerySpec querySpec = new QuerySpec(LicenseDto.class);
    querySpec.setSort(Collections.singletonList(
      new SortSpec(Collections.singletonList("createdOn"), Direction.ASC)));

    Assertions.assertThrows(UnknownAttributeException.class, 
      () -> supportedLicenseRepository
      .findAll(querySpec)); 
   
  }


}
