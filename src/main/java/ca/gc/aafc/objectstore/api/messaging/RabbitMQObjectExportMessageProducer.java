package ca.gc.aafc.objectstore.api.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.messaging.message.ObjectExportNotification;
import ca.gc.aafc.dina.messaging.producer.RabbitMQMessageProducer;
import ca.gc.aafc.objectstore.api.config.ObjectExportQueueProperties;

/**
 * RabbitMQ based message producer.
 *
 */
@Service
@ConditionalOnProperty(prefix = "dina.messaging", name = "isProducer", havingValue = "true")
public class RabbitMQObjectExportMessageProducer extends RabbitMQMessageProducer<ObjectExportNotification> implements
  ObjectExportMessageProducer {

  public RabbitMQObjectExportMessageProducer(RabbitTemplate rabbitTemplate, ObjectExportQueueProperties queueProperties) {
    super(rabbitTemplate, queueProperties);
  }

}
