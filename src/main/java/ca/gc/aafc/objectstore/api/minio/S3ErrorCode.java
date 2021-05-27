package ca.gc.aafc.objectstore.api.minio;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

@RequiredArgsConstructor
@Getter
public enum S3ErrorCode {

  NO_SUCH_KEY("NoSuchKey", "404"),
  NO_SUCH_BUCKET("NoSuchBucket", "404");

  private final String errorCode;
  private final String httpStatusCode;

  /**
   * Get an optional S3ErrorCode from a errorCode (case-insensitive)
   * @param errorCode
   * @return
   */
  public static Optional<S3ErrorCode> fromErrorCode(String errorCode) {
    if (StringUtils.isBlank(errorCode)) {
      return Optional.empty();
    }

    for (S3ErrorCode curr : S3ErrorCode.values()) {
      if (curr.getErrorCode().equalsIgnoreCase(errorCode)) {
        return Optional.of(curr);
      }
    }
    return Optional.empty();
  }

}
