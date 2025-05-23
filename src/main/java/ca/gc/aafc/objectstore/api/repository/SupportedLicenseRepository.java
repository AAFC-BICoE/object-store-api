package ca.gc.aafc.objectstore.api.repository;

import ca.gc.aafc.objectstore.api.SupportedLicensesConfiguration;
import ca.gc.aafc.objectstore.api.dto.LicenseDto;
import io.crnk.core.engine.internal.utils.PropertyException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ReadOnlyResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ca.gc.aafc.dina.exception.UnknownAttributeException;

@Component
public class SupportedLicenseRepository
  extends ReadOnlyResourceRepositoryBase<LicenseDto, String> {

  @Autowired
  private SupportedLicensesConfiguration licenses;

  protected SupportedLicenseRepository() {
    super(LicenseDto.class);
  }

  @Override
  public ResourceList<LicenseDto> findAll(QuerySpec query) {
    try {
      return query.apply(licenses.getLicenses().values());
    } catch (PropertyException propertyException) {
      throw new UnknownAttributeException(propertyException);
    }
  }
}
