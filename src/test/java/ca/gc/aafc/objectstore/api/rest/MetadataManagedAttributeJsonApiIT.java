package ca.gc.aafc.objectstore.api.rest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;

import ca.gc.aafc.objectstore.api.dto.MetadataManagedAttributeDto;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.testsupport.factories.ManagedAttributeFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;

public class MetadataManagedAttributeJsonApiIT extends BaseJsonApiIntegrationTest {

  private MetadataManagedAttributeDto mmaCreated;

  private UUID managedAttributeId;
  private UUID metadataId;

  @BeforeEach
  public void setup() {
    ManagedAttribute ma = ManagedAttributeFactory.newManagedAttribute().build();
    ObjectStoreMetadata osm = ObjectStoreMetadataFactory.newObjectStoreMetadata().build();

    // we need to run the setup in another transaction and commit it otherwise it can't be visible
    // to the test web server.
    service.runInNewTransaction(em -> {
      em.persist(ma);
      em.persist(osm);
    });

    managedAttributeId = ma.getUuid();
    metadataId = osm.getUuid();
  }

  @Override
  protected String getSchemaName() {
    return null;
  }

  @Override
  protected String getResourceUnderTest() {
    return "metadata-managed-attribute";
  }

  @Override
  protected Map<String, Object> buildCreateAttributeMap() {

    mmaCreated = new MetadataManagedAttributeDto();
    mmaCreated.setUuid(null);
    mmaCreated.setManagedAttribute(null);
    mmaCreated.setObjectStoreMetadata(null);
    mmaCreated.setAssignedValue("test value");

    return toAttributeMap(mmaCreated);
  }

  @Override
  protected Map<String, Object> buildUpdateAttributeMap() {
    mmaCreated.setAssignedValue("zxy");
    return toAttributeMap(mmaCreated);
  }

  @Override
  protected List<Relationship> buildRelationshipList() {
    return Arrays.asList(
        Relationship.of("managedAttribute", "managed-attribute", managedAttributeId.toString()),
        Relationship.of("objectStoreMetadata", "metadata", metadataId.toString()));
  }

}
