package ca.gc.aafc.objectstore.api.messaging;

import lombok.extern.log4j.Log4j2;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.messaging.DinaMessage;

/**
 * Log4j2 based {@link DinaMessageProducer} used mostly to run in dev mode.
 * Used with @ConditionalOnMissingBean as a fallback.
 */
@Log4j2
@ConditionalOnMissingBean(RabbitMQDinaMessageProducer.class)
@Service
public class DinaMessageProducerFallback implements DinaMessageProducer {

  public void send(DinaMessage notification) {
    log.info("Message produced : {}", notification::toString);
  }
}
