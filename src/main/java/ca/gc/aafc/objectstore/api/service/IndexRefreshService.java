package ca.gc.aafc.objectstore.api.service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.messaging.message.DocumentOperationNotification;
import ca.gc.aafc.dina.messaging.message.DocumentOperationType;
import ca.gc.aafc.dina.messaging.producer.DocumentOperationNotificationMessageProducer;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;

/**
 * Service used to send a notification to refresh a document in the search
 * index.
 */
@Service
@ConditionalOnProperty(prefix = "dina.messaging", name = "isProducer", havingValue = "true")
public class IndexRefreshService {

  private static final String METADATA_SQL = "SELECT uuid FROM ObjectStoreMetadata ORDER BY id";

  private final DocumentOperationNotificationMessageProducer searchRabbitMQMessageProducer;
  private final Set<String> supportedDocumentTypes;
  private final BaseDAO baseDAO;

  public IndexRefreshService(DocumentOperationNotificationMessageProducer searchRabbitMQMessageProducer,
                             BaseDAO baseDAO) {
    this.searchRabbitMQMessageProducer = searchRabbitMQMessageProducer;

    // supported document type
    supportedDocumentTypes = Set.of(ObjectStoreMetadataDto.TYPENAME);
    this.baseDAO = baseDAO;
  }

  public void reindexDocument(String docId, String type) {

    if (!supportedDocumentTypes.contains(type)) {
      throw new IllegalStateException("Unsupported document type");
    }

    DocumentOperationNotification don = DocumentOperationNotification.builder()
        .documentId(docId)
        .documentType(type)
        .operationType(DocumentOperationType.UPDATE).build();
    searchRabbitMQMessageProducer.send(don);
  }

  /**
   * Usually the transaction boundaries are at the repository level but here we only need one for
   * reindexAll
   * @param type
   */
  @Transactional(readOnly = true)
  public void reindexAll(String type) {

    if (!supportedDocumentTypes.contains(type)) {
      throw new IllegalStateException("Unsupported document type");
    }

    Stream<UUID> objStream =
      baseDAO.streamAllByQuery(UUID.class, METADATA_SQL, null);

    objStream.forEach(uuid -> {
      DocumentOperationNotification don = DocumentOperationNotification.builder()
        .documentId(uuid.toString())
        .documentType(type)
        .operationType(DocumentOperationType.UPDATE).build();
      searchRabbitMQMessageProducer.send(don);
    });
  }
}
