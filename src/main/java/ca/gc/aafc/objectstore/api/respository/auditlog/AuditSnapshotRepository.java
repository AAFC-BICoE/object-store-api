package ca.gc.aafc.objectstore.api.respository.auditlog;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.javers.core.Javers;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import ca.gc.aafc.dina.repository.NoLinkInformation;
import ca.gc.aafc.objectstore.api.dto.AuditSnapshotDto;
import ca.gc.aafc.objectstore.api.service.AuditService;
import io.crnk.core.exception.MethodNotAllowedException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ReadOnlyResourceRepositoryBase;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.resource.meta.DefaultPagedMetaInformation;
import io.crnk.core.resource.meta.PagedMetaInformation;
import lombok.NonNull;

@Repository
public class AuditSnapshotRepository extends ReadOnlyResourceRepositoryBase<AuditSnapshotDto, Long> {
  private final Javers javers;
  private final NamedParameterJdbcTemplate jdbcTemplate;

  public AuditSnapshotRepository(Javers javers, NamedParameterJdbcTemplate jdbcTemplate) {
    super(AuditSnapshotDto.class);
    this.javers = javers;
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public ResourceList<AuditSnapshotDto> findAll(QuerySpec qs) {
    int limit = Optional.ofNullable(qs.getLimit()).orElse(100L).intValue();
    int skip = Optional.ofNullable(qs.getOffset()).orElse(0L).intValue();

    Map<String, String> filters = getFilterMap(qs);
    String authorFilter = filters.get("author");
    String instanceFilter = filters.get("instanceId");

    String id = null;
    String type = null;
    
    if (StringUtils.isNotBlank(instanceFilter)) {
      String[] idAndType = getIdAndType(instanceFilter);
      id = idAndType[0];
      type = idAndType[1];
    } 

    List<AuditSnapshotDto> dtos = AuditService.findAll(javers, type, id, authorFilter, limit, skip)
      .stream().map(this::toDto).collect(Collectors.toList());

    PagedMetaInformation meta = getMetadata(authorFilter, id, type);

    return new DefaultResourceList<>(dtos, meta, new NoLinkInformation());
  }

  @Override
  public AuditSnapshotDto findOne(Long id, QuerySpec querySpec) {
    // Disable findOne.
    throw new MethodNotAllowedException("method not allowed");
  }

  /** Converts Javers snapshot to our DTO format. */
  private AuditSnapshotDto toDto(CdoSnapshot original) {
    // Get the snapshot state as a map:
    Map<String, Object> state = new HashMap<>();
    original.getState().forEachProperty((key, val) -> state.put(key, val));

    // Get the commit date as OffsetDateTime:
    OffsetDateTime commitDateTime = original.getCommitMetadata().getCommitDate().atOffset(OffsetDateTime.now().getOffset());

    return AuditSnapshotDto.builder()
        .id(original.getGlobalId().value() + "/" + commitDateTime)   
        .instanceId(original.getGlobalId().value())
        .state(state)
        .changedProperties(original.getChanged())
        .snapshotType(original.getType().toString())
        .version(original.getVersion())
        .author(original.getCommitMetadata().getAuthor())
        .commitDateTime(commitDateTime)
        .build();
  }

  /** Get the meta information with the total resource count. */
  private PagedMetaInformation getMetadata(String authorFilter, String id, String type) {
    // Use sql to get the count because Javers does not provide a counting method:
    SqlParameterSource parameters = new MapSqlParameterSource()
      .addValue("author", authorFilter)
      .addValue("id", "\"" + id + "\"") // Javers puts double-quotes around the id in the database.
      .addValue("type", type);
    
    // Apply filters:
    String baseSql = "select count(*) from jv_snapshot s join jv_commit c on s.commit_fk = c.commit_pk where 1=1 %s %s ;";
    String sql = String.format(
      baseSql,
      StringUtils.isNotBlank(authorFilter) ? "and c.author = :author" : "",
      StringUtils.isNotBlank(id) ? "and global_id_fk = (select global_id_pk from jv_global_id where local_id = :id and type_name = :type)" : ""
    );

    Long count = jdbcTemplate.queryForObject(sql, parameters, Long.class);

    DefaultPagedMetaInformation meta = new DefaultPagedMetaInformation();
    meta.setTotalResourceCount(count);

    return meta;
  }

  /**
   * Parses the id and type from the instanceFilter format.
   * e.g. metadata/42f1d751-f3ec-4c3b-9247-750f4e48ae04
   */
  private String[] getIdAndType(@NonNull String instanceFilter) {
    String[] split = instanceFilter.split("/");
    if (split.length != 2) {
      throw new IllegalArgumentException("Invalid ID must be formatted as {type}/{id}: " + instanceFilter);
    }
    String type = split[0];
    String id = split[1];
    return new String[]{ id, type };
  }

  /**
   * Converts Crnk's filters into a String/String Map.
   */
  private Map<String, String> getFilterMap(QuerySpec qs) {
    Map<String, String> map = new HashMap<>();
    qs.getFilters().forEach(
      it -> map.put(
        it.getPath().toString(),
        it.getValue().toString()
      )
    );
    return map;
  }

}
