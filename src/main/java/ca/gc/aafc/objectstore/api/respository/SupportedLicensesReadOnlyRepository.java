package ca.gc.aafc.objectstore.api.respository;

import ca.gc.aafc.objectstore.api.entities.License;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ReadOnlyResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SupportedLicensesReadOnlyRepository
    extends ReadOnlyResourceRepositoryBase<License, String> {

  @Autowired
  private List<License> licenses;

  protected SupportedLicensesReadOnlyRepository() {
    super(License.class);
  }

  @Override
  public ResourceList<License> findAll(QuerySpec query) {
    return query.apply(licenses);
  }

}