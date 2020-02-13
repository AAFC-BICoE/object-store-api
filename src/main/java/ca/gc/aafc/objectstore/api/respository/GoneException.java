package ca.gc.aafc.objectstore.api.respository;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.exception.CrnkMappableException;

public class GoneException extends CrnkMappableException {

  public GoneException(String message) {
    super(410, ErrorData.builder().setTitle(message).setDetail(message)
        .setStatus("410").build());
  }


}
