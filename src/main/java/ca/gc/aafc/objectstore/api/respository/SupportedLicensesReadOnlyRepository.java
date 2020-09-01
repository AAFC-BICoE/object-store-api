package ca.gc.aafc.objectstore.api.respository;

import ca.gc.aafc.objectstore.api.SupportedLicensesConfiguration;
import ca.gc.aafc.objectstore.api.dto.LicenseDto;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ReadOnlyResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class SupportedLicensesReadOnlyRepository
    extends ReadOnlyResourceRepositoryBase<LicenseDto, String> {

  @Autowired
  private SupportedLicensesConfiguration licenses;

  protected SupportedLicensesReadOnlyRepository() {
    super(LicenseDto.class);
  }

  @Override
  public ResourceList<LicenseDto> findAll(QuerySpec query) {
    return query.apply(licenses.getLicenses().values());
  }
}
