package ca.gc.aafc.objectstore.api.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.javers.core.Javers;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.javers.repository.jql.QueryBuilder;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

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

  /**
   * Get the meta information with the total resource count.
   * 
   * @param jdbcTemplate
   * @param authorFilter
   * @param id
   * @param type
   * @return
   */
  public static Long getResouceCount(NamedParameterJdbcTemplate jdbcTemplate, String authorFilter,
      String id, String type) {
    // Use sql to get the count because Javers does not provide a counting method:
    SqlParameterSource parameters = new MapSqlParameterSource().addValue("author", authorFilter)
        .addValue("id", "\"" + id + "\"") // Javers puts double-quotes around the id in the database.
        .addValue("type", type);

    // Apply filters:
    String baseSql = "select count(*) from jv_snapshot s join jv_commit c on s.commit_fk = c.commit_pk where 1=1 %s %s ;";
    String sql = 
      String.format(
        baseSql,
        StringUtils.isNotBlank(authorFilter) ? "and c.author = :author" : "",
        StringUtils.isNotBlank(id)
            ? "and global_id_fk = (select global_id_pk from jv_global_id where local_id = :id and type_name = :type)"
            : "");

    return jdbcTemplate.queryForObject(sql, parameters, Long.class);
  }

}
