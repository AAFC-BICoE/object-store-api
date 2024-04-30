package ca.gc.aafc.objectstore.api.messaging;

import javax.inject.Named;
import lombok.extern.log4j.Log4j2;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.messaging.message.DocumentOperationNotification;
import ca.gc.aafc.dina.messaging.producer.DocumentOperationNotificationMessageProducer;
import ca.gc.aafc.dina.messaging.producer.RabbitMQMessageProducer;
import ca.gc.aafc.objectstore.api.config.SearchQueueProperties;

/**
 * RabbitMQ based message producer
 */
@Log4j2
@Service
@ConditionalOnProperty(prefix = "dina.messaging", name = "isProducer", havingValue = "true")
public class RabbitMQSearchMessageProducer extends RabbitMQMessageProducer implements
  DocumentOperationNotificationMessageProducer {

  public RabbitMQSearchMessageProducer(RabbitTemplate rabbitTemplate,
                                       @Named("searchQueueProperties")
                                       SearchQueueProperties queueProperties) {
    super(rabbitTemplate, queueProperties);
    log.info("Using RabbitMQ queue {}", queueProperties::getQueue);
  }

  @Override
  public void send(DocumentOperationNotification message) {
    super.send(message);
  }
}
