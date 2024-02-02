package ca.gc.aafc.objectstore.api.messaging;

import lombok.extern.log4j.Log4j2;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.messaging.message.ObjectExportNotification;

/**
 * Log4j2 based {@link ObjectExportMessageProducer} used mostly to run in dev mode.
 * Used with @ConditionalOnMissingBean as a fallback.
 */
@Log4j2
@ConditionalOnMissingBean(RabbitMQObjectExportMessageProducer.class)
@Service
public class ObjectExportMessageProducerFallback implements ObjectExportMessageProducer {

  public void send(ObjectExportNotification documentOperationNotification) {
    log.info("Message produced : {}", documentOperationNotification::toString);
  }
}
