package ca.gc.aafc.objectstore.api.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.javers.core.Javers;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.javers.repository.jql.QueryBuilder;

public class AuditService {

  public static List<CdoSnapshot> findAll(Javers javers, String type, String id, String author, int limit, int skip) {
    QueryBuilder queryBuilder;

    if (StringUtils.isNotBlank(type) && StringUtils.isNotBlank(id)) {
      queryBuilder = QueryBuilder.byInstanceId(id, type);
    } else {
      queryBuilder = QueryBuilder.anyDomainObject();
    }

    if (StringUtils.isNotBlank(author)) {
      queryBuilder.byAuthor(author);
    }

    queryBuilder.limit(limit);
    queryBuilder.skip(skip);

    return javers.findSnapshots(queryBuilder.build());
  }

}
