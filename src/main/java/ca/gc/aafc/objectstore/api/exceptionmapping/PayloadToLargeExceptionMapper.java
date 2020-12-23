package ca.gc.aafc.objectstore.api.exceptionmapping;

import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class PayloadToLargeExceptionMapper {

  @ResponseStatus(value = HttpStatus.PAYLOAD_TOO_LARGE, reason = "Payload to large")
  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public void maxUploadSizeExceededException() {
    //do nothing
  }

  @ResponseStatus(value = HttpStatus.PAYLOAD_TOO_LARGE, reason = "Payload to large")
  @ExceptionHandler(FileSizeLimitExceededException.class)
  public void fileSizeLimitExceededException() {
    //do nothing
  }
}
