package ca.gc.aafc.objectstore.api.exceptionmapping;

import java.util.stream.Collectors;

import javax.inject.Named;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.engine.http.HttpStatus;

@Named
public class IllegalArgumentExceptionMapper implements ExceptionMapper<IllegalArgumentException> {

  private static final int HTTP_ERROR_CODE = HttpStatus.BAD_REQUEST_400;
  private static final String ERROR_TITLE = "Bad Request";

  @Override
  public ErrorResponse toErrorResponse(IllegalArgumentException exception) {
    ErrorData errorData = ErrorData.builder()
      .setDetail(exception.getMessage())
      .setTitle(ERROR_TITLE)
      .setStatus(Integer.toString(HTTP_ERROR_CODE))
      .build();
    return ErrorResponse.builder().setSingleErrorData(errorData).setStatus(HTTP_ERROR_CODE).build();
  }

  @Override
  public IllegalArgumentException fromErrorResponse(ErrorResponse errorResponse) {
    String errorMessage = errorResponse.getErrors()
      .stream()
      .map(ErrorData::getDetail)
      .collect(Collectors.joining(System.lineSeparator()));
    return new IllegalArgumentException(errorMessage);
  }

  @Override
  public boolean accepts(ErrorResponse errorResponse) {
    return errorResponse.getHttpStatus() == HTTP_ERROR_CODE;
  }

}
