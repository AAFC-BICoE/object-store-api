package ca.gc.aafc.objectstore.api.service;

import java.util.Set;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.messaging.message.DocumentOperationNotification;
import ca.gc.aafc.dina.messaging.message.DocumentOperationType;
import ca.gc.aafc.dina.messaging.producer.DocumentOperationNotificationMessageProducer;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;

/**
 * Service used to send a notification to refresh a document in the search index.
 */
@Service
@ConditionalOnProperty(prefix = "dina.messaging", name = "isProducer", havingValue = "true")
public class IndexRefreshService {

  private final DocumentOperationNotificationMessageProducer searchRabbitMQMessageProducer;
  private final Set<String> supportedDocumentTypes;

  public IndexRefreshService(DocumentOperationNotificationMessageProducer searchRabbitMQMessageProducer) {
    this.searchRabbitMQMessageProducer = searchRabbitMQMessageProducer;

    // supported document type
    supportedDocumentTypes = Set.of(ObjectStoreMetadataDto.TYPENAME);
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
}