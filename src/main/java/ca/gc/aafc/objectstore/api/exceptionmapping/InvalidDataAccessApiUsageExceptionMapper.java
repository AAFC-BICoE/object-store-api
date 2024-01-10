package ca.gc.aafc.objectstore.api.exceptionmapping;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.engine.http.HttpStatus;
import java.util.stream.Collectors;
import javax.inject.Named;

import org.springframework.dao.InvalidDataAccessApiUsageException;

@Named
public class InvalidDataAccessApiUsageExceptionMapper implements ExceptionMapper<InvalidDataAccessApiUsageException> {

  private static final int HTTP_ERROR_CODE = HttpStatus.BAD_REQUEST_400;
  private static final String ERROR_TITLE = "Bad Request";

  @Override
  public ErrorResponse toErrorResponse(InvalidDataAccessApiUsageException exception) {
    Throwable source = exception.getCause();
    ErrorData errorData = ErrorData.builder()
      .setDetail(source.getMessage())
      .setTitle(ERROR_TITLE)
      .setStatus(Integer.toString(HTTP_ERROR_CODE))
      .build();
    return ErrorResponse.builder().setSingleErrorData(errorData).setStatus(HTTP_ERROR_CODE).build();
  }

  @Override
  public InvalidDataAccessApiUsageException fromErrorResponse(ErrorResponse errorResponse) {
    String errorMessage = errorResponse.getErrors()
      .stream()
      .map(ErrorData::getDetail)
      .collect(Collectors.joining(System.lineSeparator()));
    return new InvalidDataAccessApiUsageException(errorMessage);
  }

  @Override
  public boolean accepts(ErrorResponse errorResponse) {
    return errorResponse.getHttpStatus() == HTTP_ERROR_CODE;
  }

}
