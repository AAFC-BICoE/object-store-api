package ca.gc.aafc.objectstore.api.messaging;

import lombok.extern.log4j.Log4j2;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.messaging.DinaMessage;
import ca.gc.aafc.dina.messaging.message.DocumentOperationNotification;
import ca.gc.aafc.dina.messaging.producer.DinaMessageProducer;
import ca.gc.aafc.dina.messaging.producer.DocumentOperationNotificationMessageProducer;

/**
 * Log4j2 based {@link DinaMessageProducer} used mostly to run in dev mode.
 * Used with @ConditionalOnMissingBean as a fallback.
 */
@Log4j2
@ConditionalOnMissingBean(RabbitMQObjectExportMessageProducer.class)
@Service
public class ObjectExportMessageProducerFallback implements DinaMessageProducer,
  DocumentOperationNotificationMessageProducer {

  public void send(DinaMessage dinaMessage) {
    log.info("Message produced : {}", dinaMessage::toString);
  }

  @Override
  public void send(DocumentOperationNotification message) {
    log.info("Message produced : {}", message::toString);
  }
}
