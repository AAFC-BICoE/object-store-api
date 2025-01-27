package ca.gc.aafc.objectstore.api.repository;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.messaging.message.DocumentOperationNotification;
import ca.gc.aafc.dina.messaging.producer.DocumentOperationNotificationMessageProducer;
import ca.gc.aafc.dina.security.auth.DinaAdminCUDAuthorizationService;
import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.dto.IndexRefreshDto;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.service.IndexRefreshService;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

public class IndexRefreshRepositoryIT extends BaseIntegrationTest {

  @Inject
  private BaseDAO baseDAO;

  @Inject
  private DinaAdminCUDAuthorizationService dinaAdminCUDAuthorizationService;

  @Test
  public void indexRefreshRepository_onRefreshAll_messageSent() {
    // we are not using beans to avoid the RabbitMQ part
    List<DocumentOperationNotification> messages = new ArrayList<>();
    DocumentOperationNotificationMessageProducer messageProducer = messages::add;
    IndexRefreshService service = new IndexRefreshService(messageProducer, baseDAO);
    IndexRefreshRepository repo = new IndexRefreshRepository(dinaAdminCUDAuthorizationService, service);

    ObjectUpload newObjectUpload = objectUploadService.create(ObjectUploadFactory.buildTestObjectUpload());
    ObjectStoreMetadata objectStoreMetadata = ObjectStoreMetadataFactory
      .newObjectStoreMetadata()
      .fileExtension(null)
      .acHashValue(null)
      .fileIdentifier(newObjectUpload.getFileIdentifier())
      .build();

    objectStoreMetaDataService.create(objectStoreMetadata);

    IndexRefreshDto dto = new IndexRefreshDto();
    dto.setDocType(ObjectStoreMetadataDto.TYPENAME);
    repo.handlePost(EntityModel.of(dto));

    // we may get more than 1 message if the database includes records from other tests
    assertFalse(messages.isEmpty());
  }


}
