package ca.gc.aafc.objectstore.api.exceptionmapping;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.exception.MethodNotAllowedException;

import javax.inject.Named;

@Named
public class ManagedAttributeChildConflictExceptionMapper implements ExceptionMapper<ManagedAttributeChildConflictException> {

  public static final int ERROR_CODE = Integer.parseInt(ManagedAttributeChildConflictException.HTTP_CODE);

  @Override
  public ErrorResponse toErrorResponse(ManagedAttributeChildConflictException e) {
    ErrorData errorData = ErrorData.builder()
      .setDetail(e.getMessage())
      .setTitle(ManagedAttributeChildConflictException.ERROR_TITLE)
      .setStatus(ManagedAttributeChildConflictException.HTTP_CODE)
      .build();
    return ErrorResponse.builder()
      .setSingleErrorData(errorData)
      .setStatus(ERROR_CODE)
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
