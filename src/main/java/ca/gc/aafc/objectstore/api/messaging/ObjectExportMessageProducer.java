package ca.gc.aafc.objectstore.api.messaging;

import ca.gc.aafc.dina.messaging.message.ObjectExportNotification;

/**
 * Interface used to abstract the message producer send method for ({@link ObjectExportNotification}.
 */
public interface ObjectExportMessageProducer {

   void send(ObjectExportNotification objectExportNotification);
}
