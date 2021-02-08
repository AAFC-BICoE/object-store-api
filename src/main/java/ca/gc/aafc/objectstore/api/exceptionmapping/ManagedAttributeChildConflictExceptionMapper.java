package ca.gc.aafc.objectstore.api.exceptionmapping;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.exception.MethodNotAllowedException;
import org.springframework.http.HttpStatus;

import javax.inject.Named;

@Named
public class ManagedAttributeChildConflictExceptionMapper implements ExceptionMapper<ManagedAttributeChildConflictException> {

  public static final HttpStatus HTTP_STATUS = HttpStatus.CONFLICT;

  @Override
  public ErrorResponse toErrorResponse(ManagedAttributeChildConflictException e) {
    ErrorData errorData = ErrorData.builder()
      .setDetail(e.getMessage())
      .setTitle(HTTP_STATUS.getReasonPhrase())
      .setStatus(Integer.toString(HTTP_STATUS.value()))
      .build();
    return ErrorResponse.builder()
      .setSingleErrorData(errorData)
      .setStatus(HTTP_STATUS.value())
      .build();
  }

  @Override
  public ManagedAttributeChildConflictException fromErrorResponse(ErrorResponse errorResponse) {
    throw new MethodNotAllowedException("crnk client unsupported");
  }

  @Override
  public boolean accepts(ErrorResponse errorResponse) {
    throw new MethodNotAllowedException("crnk client unsupported");
  }
}
