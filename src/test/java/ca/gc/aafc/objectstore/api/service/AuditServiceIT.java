package ca.gc.aafc.objectstore.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.javers.core.Javers;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;

public class AuditServiceIT extends BaseIntegrationTest {

  @Inject
  private Javers javers;

  @Inject
  private NamedParameterJdbcTemplate jdbcTemplate;

  @Inject
  private AuditService serviceUnderTest;

  private static final String AUTHOR = "dina_user";
  private static final String TYPE = ObjectStoreMetadataDto.TYPENAME;
  private static final UUID INSTANCE_ID = UUID.randomUUID();

  /**
   * Persists 6 snap shots in total for each test.
   */
  @BeforeEach
  public void beforeEachTest() {
    // Has Author 2 Commits
    ObjectStoreMetadataDto hasAuthor = createDto();
    javers.commit(AUTHOR, hasAuthor);
    hasAuthor.setAcCaption("update");
    javers.commit(AUTHOR, hasAuthor);

    // Anonymous Author 2 Commits
    ObjectStoreMetadataDto noAuthor = createDto();
    javers.commit("Anonymous", noAuthor);
    noAuthor.setAcCaption("update");
    javers.commit("Anonymous", noAuthor);

    // Has Author With specific instance id 2 commits
    ObjectStoreMetadataDto withInstanceID = createDto();
    withInstanceID.setUuid(INSTANCE_ID);
    javers.commit(AUTHOR, withInstanceID);
    withInstanceID.setAcCaption("update");
    javers.commit(AUTHOR, withInstanceID);
  }

  @Test
  public void findAll_whenNoFilter_AllSnapShotsReturned() {
    List<CdoSnapshot> results = serviceUnderTest.findAll(null, null, null, 10, 0);
    assertEquals(6, results.size());
  }

  @Test
  public void findAll_whenFilteredByInstance_snapshotsFiltered() {
    List<CdoSnapshot> results = serviceUnderTest.findAll(TYPE, INSTANCE_ID.toString(), null, 10, 0);
    assertEquals(2, results.size());
    results.forEach(shot -> 
      assertEquals(
        String.join("/", TYPE, INSTANCE_ID.toString()),
        shot.getGlobalId().toString()));
  }

  @Test
  public void findAll_whenFilteredByAuthor_snapshotsFiltered() {
    List<CdoSnapshot> results = serviceUnderTest.findAll(TYPE, null, AUTHOR, 10, 0);
    assertEquals(4, results.size());
    results.forEach(shot -> assertEquals(AUTHOR, shot.getCommitMetadata().getAuthor()));
  }

  @Test
  public void findAll_WithLimit_LimitsResults() {
    List<CdoSnapshot> results = serviceUnderTest.findAll(null, null, null, 1, 0);
    assertEquals(1, results.size());
  }

  @Test
  public void findAll_WithOffset_ResultsOffset() {
    List<CdoSnapshot> results = serviceUnderTest.findAll(null, null, null, 10, 5);
    assertEquals(1, results.size());
  }

  private static ObjectStoreMetadataDto createDto() {
    ObjectStoreMetadataDto dto = new ObjectStoreMetadataDto();
    dto.setUuid(UUID.randomUUID());
    return dto;
  }
}
