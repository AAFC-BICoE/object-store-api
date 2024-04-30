package ca.gc.aafc.objectstore.api.messaging;

import lombok.extern.log4j.Log4j2;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.messaging.producer.DinaMessageProducer;
import ca.gc.aafc.dina.messaging.producer.RabbitMQMessageProducer;
import ca.gc.aafc.objectstore.api.config.ObjectExportQueueProperties;

/**
 * RabbitMQ based message producer.
 *
 */
@Log4j2
@Service
@ConditionalOnProperty(prefix = "dina.messaging", name = "isProducer", havingValue = "true")
public class RabbitMQDinaMessageProducer extends RabbitMQMessageProducer implements DinaMessageProducer {

  public RabbitMQDinaMessageProducer(RabbitTemplate rabbitTemplate, ObjectExportQueueProperties queueProperties) {
    super(rabbitTemplate, queueProperties);
    log.info( "Using RabbitMQ queue {}", queueProperties::getQueue);
  }

}
