package ca.gc.aafc.objectstore.api.minio;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum S3ErrorCodes {

  NO_SUCH_KEY("NoSuchKey", "404"),
  NO_SUCH_BUCKET("NoSuchBucket", "404");

  private final String errorCode;
  private final String httpStatusCode;

}
